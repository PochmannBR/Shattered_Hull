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


package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FabricatorSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

//Shattered Hull: histórico de versões do próprio jogo
public class HullChanges {

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add( changeInfos, "v0.4.0", new ChangeButton[]{
				new ChangeButton(new GooSprite(), msg("v040_boss_title"), msg("v040_boss")),
				new ChangeButton(Icons.get(Icons.TALENT), msg("v040_enemies_title"), msg("v040_enemies")),
				new ChangeButton(Icons.get(Icons.WARNING), msg("v040_continue_title"), msg("v040_continue")),
				new ChangeButton(Icons.get(Icons.PREFS), msg("v040_balance_title"), msg("v040_balance")),
				new ChangeButton(Icons.get(Icons.ENTER), msg("v040_classes_title"), msg("v040_classes")),
		});
		add( changeInfos, "v0.3.0", new ChangeButton[]{
				new ChangeButton(new FabricatorSprite(), msg("v030_fab_title"), msg("v030_fab")),
				new ChangeButton(Icons.get(Icons.INFO), msg("v030_credits_title"), msg("v030_credits")),
		});
		add( changeInfos, "v0.2.0", new ChangeButton[]{
				new ChangeButton(new ItemSprite(ItemSpriteSheet.SERVICE_RIFLE), msg("v020_rifle_title"), msg("v020_rifle")),
				new ChangeButton(new ItemSprite(ItemSpriteSheet.GOLD), msg("v020_laztro_title"), msg("v020_laztro")),
				new ChangeButton(Icons.get(Icons.JOURNAL), msg("v020_story_title"), msg("v020_story")),
				new ChangeButton(Icons.get(Icons.DEPTH), msg("v020_env_title"), msg("v020_env")),
		});
		add( changeInfos, "v0.1.0", new ChangeButton[]{
				new ChangeButton(Icons.get(Icons.SHPX), msg("v010_base_title"), msg("v010_base")),
		});
	}

	private static void add( ArrayList<ChangeInfo> changeInfos, String version, ChangeButton[] buttons ){
		ChangeInfo changes = new ChangeInfo(version, true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		for (ChangeButton b : buttons){
			changes.addButton(b);
		}
	}

	private static String msg( String key ){
		return Messages.get(ChangesScene.class, "hull_" + key);
	}
}
