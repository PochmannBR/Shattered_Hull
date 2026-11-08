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


package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

//Shattered Hull: sprite provisório do fabricador
public class FabricatorSprite extends MobSprite {

	public FabricatorSprite() {
		super();

		texture( Assets.Sprites.FABRICATOR );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 3, true );
		idle.frames( frames, 0, 0, 1, 0, 0, 2 );

		run = idle.clone();
		attack = idle.clone();

		die = new Animation( 1, false );
		die.frames( frames, 0 );

		play( idle );
	}

	@Override
	public void link( com.shatteredpixel.shatteredpixeldungeon.actors.Char ch ) {
		super.link( ch );
		renderShadow = false;
	}
}
