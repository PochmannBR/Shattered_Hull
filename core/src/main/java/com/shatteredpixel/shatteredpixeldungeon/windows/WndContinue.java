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
import com.shatteredpixel.shatteredpixeldungeon.HullContinue;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

//Shattered Hull: janela do continue pago, mostrada quando o herói cai
public class WndContinue extends WndOptions {

	private final Object cause;

	public WndContinue( Object cause ) {
		super( Icons.WARNING.get(),
				Messages.get(WndContinue.class, "title"),
				Messages.get(WndContinue.class, "message", Dungeon.depth),
				Messages.get(WndContinue.class, "use"),
				Messages.get(WndContinue.class, "give_up") );
		this.cause = cause == null ? new HullContinue.UnknownCause() : cause;
		WndResurrect.instance = this;
	}

	@Override
	protected void onSelect( int index ) {
		if (index == 0) {
			HullContinue.requestPurchase(new HullContinue.Listener() {
				@Override
				public void onResult( boolean granted ) {
					if (granted) {
						Statistics.continueUsed = true;
						WndResurrect.instance = null;
						Dungeon.hero.continueRevive();
					} else {
						GameScene.show(new WndContinue(cause));
					}
				}
			});
		} else {
			WndResurrect.instance = null;
			Dungeon.hero.finishDeath(cause);
			//a fonte do dano já tentou registrar a morte enquanto a janela
			//estava aberta; agora o registro no ranking acontece de fato
			Dungeon.fail(cause);
		}
	}

	@Override
	public void onBackPressed() {
		//a escolha é obrigatória
	}
}
