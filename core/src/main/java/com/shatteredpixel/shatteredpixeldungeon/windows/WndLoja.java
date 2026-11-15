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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.HullStore;
import com.shatteredpixel.shatteredpixeldungeon.HullStore.Produto;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

//Shattered Hull: loja com o título de apoiador e as classes
public class WndLoja extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int BTN_H = 18;
	private static final int GAP = 2;

	private static final Produto[] ORDEM = {
			Produto.APOIADOR, Produto.PACOTE_CLASSES,
			Produto.CLASSE_EXTERMINADOR, Produto.CLASSE_FORCAS, Produto.CLASSE_PILOTO,
			Produto.CLASSE_RENEGADO, Produto.CLASSE_SINTETICO
	};

	public WndLoja() {
		super();
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle titulo = new IconTitle(Icons.GOLD.get(), Messages.get(this, "title"));
		titulo.setRect(0, 0, width, 0);
		add(titulo);
		float pos = titulo.bottom() + 2 * GAP;

		RenderedTextBlock intro = PixelScene.renderTextBlock(6);
		intro.text(Messages.get(this, HullStore.instance.disponivel() ? "intro" : "indisponivel"), width);
		intro.setPos(0, pos);
		add(intro);
		pos = intro.bottom() + 2 * GAP;

		for (final Produto p : ORDEM) {
			RedButton b = new RedButton(rotulo(p), 6) {
				@Override
				protected void onClick() {
					abrirProduto(p);
				}
			};
			b.multiline = true;
			if (p == Produto.APOIADOR) b.icon(Icons.GOLD.get());
			b.enable(HullStore.instance.disponivel() && HullStore.aVenda(p));
			b.setRect(0, pos, width, BTN_H);
			add(b);
			pos += BTN_H + GAP;
		}

		RedButton restaurar = new RedButton(Messages.get(this, "restaurar"), 6) {
			@Override
			protected void onClick() {
				HullStore.instance.restaurar(new HullStore.Callback1() {
					@Override
					public void resultado( HullStore.Resultado r ) {
						reabrir();
						ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndLoja.class,
								r == HullStore.Resultado.COMPRADO ? "restaurado" : "falhou")));
					}
				});
			}
		};
		restaurar.enable(HullStore.instance.disponivel());
		restaurar.setRect(0, pos + GAP, width, BTN_H);
		add(restaurar);
		pos = restaurar.bottom() + GAP;

		if (HullStore.instance.simulada()) {
			RedButton apagar = new RedButton(Messages.get(this, "apagar_simuladas"), 6) {
				@Override
				protected void onClick() {
					for (Produto p : Produto.values()) HullStore.registrar(p, false);
					reabrir();
				}
			};
			apagar.setRect(0, pos, width, BTN_H);
			add(apagar);
			pos = apagar.bottom() + GAP;
		}

		resize(width, (int) pos - GAP);
	}

	private void reabrir() {
		hide();
		ShatteredPixelDungeon.scene().addToFront(new WndLoja());
	}

	public static String nome( Produto p ) {
		if (p.classe != null) return Messages.titleCase(p.classe.title());
		return Messages.get(WndLoja.class, p.id);
	}

	private static String rotulo( Produto p ) {
		String estado;
		if (p.classe != null && !HullStore.classePronta(p.classe)) {
			estado = Messages.get(WndLoja.class, "em_breve");
		} else if (p == Produto.PACOTE_CLASSES && !HullStore.aVenda(p)) {
			estado = Messages.get(WndLoja.class, todasProntasCompradas() ? "adquirido" : "em_breve");
		} else if (!HullStore.aVenda(p)) {
			estado = Messages.get(WndLoja.class, "adquirido");
		} else {
			String preco = HullStore.instance.preco(p);
			estado = preco == null ? "..." : preco;
		}
		return nome(p) + ": " + estado;
	}

	private static boolean todasProntasCompradas() {
		boolean alguma = false;
		for (HeroClass cl : HeroClass.values()) {
			if (cl == HeroClass.WARRIOR || !HullStore.classePronta(cl)) continue;
			alguma = true;
			if (!HullStore.possuiClasse(cl)) return false;
		}
		return alguma;
	}

	private void abrirProduto( final Produto p ) {
		String preco = HullStore.instance.preco(p);
		String desc = p.classe != null
				? Messages.get(WndLoja.class, "classe_desc", nome(p), HullStore.TESTE_ATE_SETOR)
				: Messages.get(WndLoja.class, p.id + "_desc");
		ShatteredPixelDungeon.scene().addToFront(new WndOptions(Icons.GOLD.get(), nome(p), desc,
				Messages.get(WndLoja.class, "comprar", preco == null ? "" : preco),
				Messages.get(WndLoja.class, "voltar")) {
			@Override
			protected void onSelect( int index ) {
				if (index != 0) return;
				comprar(p, new HullStore.Callback1() {
					@Override
					public void resultado( HullStore.Resultado r ) {
						reabrir();
					}
				});
			}
		});
	}

	//compra com as mensagens de resultado; usado também pelo fim do teste e pela seleção de classe
	public static void comprar( final Produto p, final HullStore.Callback1 depois ) {
		HullStore.instance.comprar(p, new HullStore.Callback1() {
			@Override
			public void resultado( HullStore.Resultado r ) {
				String chave = null;
				switch (r) {
					case COMPRADO: chave = p == Produto.APOIADOR ? "obrigado_apoiador" : "obrigado"; break;
					case PENDENTE: chave = "pendente"; break;
					case ERRO: case INDISPONIVEL: chave = "falhou"; break;
					default: break;
				}
				if (depois != null) depois.resultado(r);
				if (chave != null) {
					ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndLoja.class, chave)));
				}
			}
		});
	}
}
