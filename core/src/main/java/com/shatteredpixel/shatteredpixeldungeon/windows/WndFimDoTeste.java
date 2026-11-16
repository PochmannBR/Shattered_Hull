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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.HullStore;
import com.shatteredpixel.shatteredpixeldungeon.HullStore.Produto;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;

import java.io.IOException;

//Shattered Hull: aparece quando uma classe em teste tenta passar do setor 5.
//Com a escada, o jogador pode voltar e continuar no setor; sem ela, por exemplo
//depois de cair num buraco, a única saída sem compra é voltar ao menu.
public class WndFimDoTeste extends WndOptions {

	private final LevelTransition escada;

	public WndFimDoTeste( LevelTransition escada ) {
		super(Icons.WARNING.get(),
				Messages.get(WndFimDoTeste.class, "title"),
				Messages.get(WndFimDoTeste.class, "message",
						Messages.titleCase(Dungeon.hero.heroClass.title()), HullStore.TESTE_ATE_SETOR),
				Messages.get(WndFimDoTeste.class, "comprar_classe", preco(Produto.daClasse(Dungeon.hero.heroClass))),
				Messages.get(WndFimDoTeste.class, "comprar_pacote", preco(Produto.PACOTE_CLASSES)),
				Messages.get(WndFimDoTeste.class, escada != null ? "voltar" : "menu"));
		this.escada = escada;
	}

	private static String preco( Produto p ) {
		String s = p == null ? null : HullStore.instance.preco(p);
		return s == null ? "" : s;
	}

	@Override
	protected boolean enabled( int index ) {
		return index == 2 || HullStore.instance.disponivel();
	}

	@Override
	protected void onSelect( int index ) {
		final HeroClass cl = Dungeon.hero.heroClass;
		if (index == 0 || index == 1) {
			Produto p = index == 0 ? Produto.daClasse(cl) : Produto.PACOTE_CLASSES;
			WndLoja.comprar(p, new HullStore.Callback1() {
				@Override
				public void resultado( HullStore.Resultado r ) {
					if (HullStore.possuiClasse(cl)) {
						if (escada != null) Dungeon.level.activateTransition(Dungeon.hero, escada);
					} else {
						GameScene.show(new WndFimDoTeste(escada));
					}
				}
			});
		} else if (escada == null) {
			try {
				Dungeon.saveAll();
			} catch (IOException e) {
				ShatteredPixelDungeon.reportException(e);
			}
			Game.switchScene(TitleScene.class);
		}
	}

	@Override
	public void onBackPressed() {
		if (escada != null) super.onBackPressed();
	}
}
