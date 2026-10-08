package com.onebrain.game.monetization;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.onebrain.game.BuildConfig;
import com.onebrain.game.util.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Google Play Billing wrapper. Handles connection with back-off, product
 * queries, purchase flow, acknowledgement (non-consumables), consumption
 * (coin packs) and restoring purchases. All callbacks are marshalled to the
 * main thread before touching game state.
 */
public final class BillingManager implements PurchasesUpdatedListener, BillingClientStateListener {

    public enum PurchaseEvent {
        SUCCESS,
        PENDING,
        ERROR,
        RESTORED,
        UNAVAILABLE
    }

    /** Grants the entitlement for a product. Must be idempotent for non-consumables. */
    public interface EntitlementGrantor {
        void grant(String productId);
    }

    private static final long MAX_RECONNECT_DELAY_MS = 60_000L;

    private final BillingClient client;
    private final EntitlementGrantor grantor;
    private final PurchaseVerifier verifier = new PurchaseVerifier(BuildConfig.PLAY_LICENSE_KEY);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Map<String, ProductDetails>> products =
            new MutableLiveData<>(Collections.emptyMap());
    private final MutableLiveData<Event<PurchaseEvent>> events = new MutableLiveData<>();
    private final Set<String> tokensInFlight = new HashSet<>();
    private long reconnectDelayMs = 1_000L;
    private boolean connecting;

    public BillingManager(Context context, EntitlementGrantor grantor) {
        this.grantor = grantor;
        client = BillingClient.newBuilder(context.getApplicationContext())
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
    }

    public void connect() {
        if (client.isReady()) {
            return;
        }
        if (connecting) {
            return;
        }
        connecting = true;
        client.startConnection(this);
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult result) {
        main.post(() -> {
            connecting = false;
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                reconnectDelayMs = 1_000L;
                queryProducts();
                queryPurchases(false);
            }
        });
    }

    @Override
    public void onBillingServiceDisconnected() {
        main.post(() -> {
            connecting = false;
            main.postDelayed(this::connect, reconnectDelayMs);
            reconnectDelayMs = Math.min(MAX_RECONNECT_DELAY_MS, reconnectDelayMs * 2);
        });
    }

    public LiveData<Map<String, ProductDetails>> getProducts() {
        return products;
    }

    public LiveData<Event<PurchaseEvent>> getEvents() {
        return events;
    }

    /** Localized price such as "$1.99", or {@code null} while unknown. */
    @Nullable
    public String getFormattedPrice(String productId) {
        Map<String, ProductDetails> map = products.getValue();
        ProductDetails details = map != null ? map.get(productId) : null;
        if (details == null) {
            return null;
        }
        ProductDetails.OneTimePurchaseOfferDetails offer = details.getOneTimePurchaseOfferDetails();
        return offer != null ? offer.getFormattedPrice() : null;
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();
        for (String id : ProductCatalog.ALL) {
            list.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(list).build();
        client.queryProductDetailsAsync(params, (result, detailsResult) -> {
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                return;
            }
            Map<String, ProductDetails> map = new HashMap<>();
            for (ProductDetails d : detailsResult.getProductDetailsList()) {
                map.put(d.getProductId(), d);
            }
            products.postValue(Collections.unmodifiableMap(map));
        });
    }

    public void launchPurchase(Activity activity, String productId) {
        Map<String, ProductDetails> map = products.getValue();
        ProductDetails details = map != null ? map.get(productId) : null;
        if (!client.isReady() || details == null) {
            events.setValue(new Event<>(PurchaseEvent.UNAVAILABLE));
            connect();
            return;
        }
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()))
                .build();
        BillingResult result = client.launchBillingFlow(activity, params);
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK
                && result.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED) {
            events.setValue(new Event<>(PurchaseEvent.ERROR));
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult result, @Nullable List<Purchase> purchases) {
        main.post(() -> {
            int code = result.getResponseCode();
            if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase p : purchases) {
                    handlePurchase(p, false);
                }
            } else if (code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                queryPurchases(true);
            } else if (code != BillingClient.BillingResponseCode.USER_CANCELED) {
                events.setValue(new Event<>(PurchaseEvent.ERROR));
            }
        });
    }

    public void restorePurchases() {
        if (!client.isReady()) {
            events.setValue(new Event<>(PurchaseEvent.UNAVAILABLE));
            connect();
            return;
        }
        queryPurchases(true);
    }

    private void queryPurchases(boolean userInitiated) {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        client.queryPurchasesAsync(params, (result, list) -> main.post(() -> {
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                if (userInitiated) {
                    events.setValue(new Event<>(PurchaseEvent.ERROR));
                }
                return;
            }
            for (Purchase p : list) {
                handlePurchase(p, true);
            }
            if (userInitiated) {
                events.setValue(new Event<>(PurchaseEvent.RESTORED));
            }
        }));
    }

    private void handlePurchase(Purchase purchase, boolean restored) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            if (!restored) {
                events.setValue(new Event<>(PurchaseEvent.PENDING));
            }
            return;
        }
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            return;
        }
        if (!verifier.isValid(purchase.getOriginalJson(), purchase.getSignature())) {
            events.setValue(new Event<>(PurchaseEvent.ERROR));
            return;
        }
        String token = purchase.getPurchaseToken();
        if (!tokensInFlight.add(token)) {
            return;
        }
        boolean consumable = false;
        for (String id : purchase.getProducts()) {
            consumable |= ProductCatalog.isConsumable(id);
        }
        if (consumable) {
            // Grant only after Play confirms consumption, so coins can never be granted twice.
            ConsumeParams params = ConsumeParams.newBuilder().setPurchaseToken(token).build();
            client.consumeAsync(params, (result, consumedToken) -> main.post(() -> {
                tokensInFlight.remove(token);
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (String id : purchase.getProducts()) {
                        grantor.grant(id);
                    }
                    events.setValue(new Event<>(PurchaseEvent.SUCCESS));
                } else if (!restored) {
                    events.setValue(new Event<>(PurchaseEvent.ERROR));
                }
            }));
            return;
        }
        for (String id : purchase.getProducts()) {
            grantor.grant(id);
        }
        if (!restored) {
            events.setValue(new Event<>(PurchaseEvent.SUCCESS));
        }
        if (purchase.isAcknowledged()) {
            tokensInFlight.remove(token);
        } else {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build();
            client.acknowledgePurchase(params, result -> main.post(() -> tokensInFlight.remove(token)));
        }
    }
}
