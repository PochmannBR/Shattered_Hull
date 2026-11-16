/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.shatteredpixel.shatteredpixeldungeon.HullStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Shattered Hull: compras pela Google Play Billing Library 8.
//Os produtos têm que existir no Play Console com os mesmos ids de HullStore.Produto,
//todos como produtos únicos (in-app). O continue é consumido logo depois da compra;
//os outros são reconhecidos e ficam na conta do jogador.
public class AndroidHullStore extends HullStore implements PurchasesUpdatedListener {

	private final Activity activity;
	private final BillingClient cliente;
	private final Map<String, ProductDetails> detalhes = new HashMap<>();
	private boolean conectado = false;

	//compra em andamento, para devolver o resultado a quem pediu
	private Produto pedido;
	private Callback1 retorno;

	public AndroidHullStore( Activity activity ) {
		this.activity = activity;
		cliente = BillingClient.newBuilder(activity)
				.setListener(this)
				.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
				.enableAutoServiceReconnection()
				.build();
		conectar(null);
	}

	private void conectar( final Runnable depois ) {
		cliente.startConnection(new BillingClientStateListener() {
			@Override
			public void onBillingSetupFinished( BillingResult r ) {
				conectado = r.getResponseCode() == BillingClient.BillingResponseCode.OK;
				if (conectado) {
					carregarProdutos();
					consultarCompras(null);
				}
				if (depois != null) depois.run();
			}

			@Override
			public void onBillingServiceDisconnected() {
				conectado = false;
			}
		});
	}

	private void carregarProdutos() {
		List<QueryProductDetailsParams.Product> lista = new ArrayList<>();
		for (Produto p : Produto.values()) {
			lista.add(QueryProductDetailsParams.Product.newBuilder()
					.setProductId(p.id)
					.setProductType(BillingClient.ProductType.INAPP)
					.build());
		}
		cliente.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(lista).build(),
				new ProductDetailsResponseListener() {
					@Override
					public void onProductDetailsResponse( BillingResult r, QueryProductDetailsResult resultado ) {
						if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
						synchronized (detalhes) {
							for (ProductDetails d : resultado.getProductDetailsList()) {
								detalhes.put(d.getProductId(), d);
							}
						}
					}
				});
	}

	//consulta as compras já feitas; ajusta o que o jogador tem, inclusive reembolsos
	private void consultarCompras( final Callback1 cb ) {
		cliente.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
				new PurchasesResponseListener() {
					@Override
					public void onQueryPurchasesResponse( BillingResult r, List<Purchase> compras ) {
						if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) {
							entregar(cb, Resultado.ERRO);
							return;
						}
						List<String> pagos = new ArrayList<>();
						for (Purchase c : compras) {
							if (c.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
							pagos.addAll(c.getProducts());
							processar(c, null, null);
						}
						for (Produto p : Produto.values()) {
							if (!p.consumivel) registrar(p, pagos.contains(p.id));
						}
						entregar(cb, Resultado.COMPRADO);
					}
				});
	}

	@Override
	public boolean disponivel() {
		return conectado;
	}

	@Override
	public String preco( Produto p ) {
		ProductDetails d;
		synchronized (detalhes) {
			d = detalhes.get(p.id);
		}
		if (d == null || d.getOneTimePurchaseOfferDetails() == null) return null;
		return d.getOneTimePurchaseOfferDetails().getFormattedPrice();
	}

	@Override
	public void comprar( final Produto p, final Callback1 cb ) {
		if (!conectado) {
			conectar(null);
			entregar(cb, Resultado.INDISPONIVEL);
			return;
		}
		final ProductDetails d;
		synchronized (detalhes) {
			d = detalhes.get(p.id);
		}
		if (d == null) {
			carregarProdutos();
			entregar(cb, Resultado.INDISPONIVEL);
			return;
		}
		pedido = p;
		retorno = cb;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BillingFlowParams params = BillingFlowParams.newBuilder()
						.setProductDetailsParamsList(Collections.singletonList(
								BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).build()))
						.build();
				BillingResult r = cliente.launchBillingFlow(activity, params);
				if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) {
					terminar(Resultado.ERRO);
				}
			}
		});
	}

	@Override
	public void restaurar( Callback1 cb ) {
		if (!conectado) {
			entregar(cb, Resultado.INDISPONIVEL);
			return;
		}
		consultarCompras(cb);
	}

	@Override
	public void onPurchasesUpdated( BillingResult r, List<Purchase> compras ) {
		int codigo = r.getResponseCode();
		if (codigo == BillingClient.BillingResponseCode.OK && compras != null) {
			for (Purchase c : compras) processar(c, pedido, retorno);
		} else if (codigo == BillingClient.BillingResponseCode.USER_CANCELED) {
			terminar(Resultado.CANCELADO);
		} else if (codigo == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
			consultarCompras(null);
			terminar(Resultado.COMPRADO);
		} else {
			terminar(Resultado.ERRO);
		}
	}

	//confirma uma compra paga: consome o continue e reconhece os outros produtos
	private void processar( final Purchase c, final Produto esperado, final Callback1 cb ) {
		if (c.getPurchaseState() == Purchase.PurchaseState.PENDING) {
			if (esperado != null && c.getProducts().contains(esperado.id)) terminar(Resultado.PENDENTE);
			return;
		}
		if (c.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;

		for (String id : c.getProducts()) {
			final Produto p = Produto.porId(id);
			if (p == null) continue;
			final boolean eraOPedido = esperado == p;
			if (p.consumivel) {
				//um continue pago e não consumido de uma sessão anterior vira crédito
				//para a próxima vez que o herói cair
				cliente.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(c.getPurchaseToken()).build(),
						new ConsumeResponseListener() {
							@Override
							public void onConsumeResponse( BillingResult r, String token ) {
								boolean ok = r.getResponseCode() == BillingClient.BillingResponseCode.OK;
								if (eraOPedido) {
									terminar(ok ? Resultado.COMPRADO : Resultado.ERRO);
								} else if (ok) {
									//continue pago numa sessão que caiu antes da entrega: vira crédito
									adicionarCreditoContinue();
								}
							}
						});
			} else {
				registrar(p, true);
				if (!c.isAcknowledged()) {
					cliente.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(c.getPurchaseToken()).build(),
							new AcknowledgePurchaseResponseListener() {
								@Override
								public void onAcknowledgePurchaseResponse( BillingResult r ) {
									//sem reconhecimento em 3 dias, a Google Play reembolsa a compra;
									//a próxima consulta de compras tenta de novo
								}
							});
				}
				if (eraOPedido) terminar(Resultado.COMPRADO);
			}
		}
	}

	private void terminar( Resultado r ) {
		Callback1 cb = retorno;
		retorno = null;
		pedido = null;
		entregar(cb, r);
	}
}
