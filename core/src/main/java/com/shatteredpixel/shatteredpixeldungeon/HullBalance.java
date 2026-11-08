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


package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;

//Shattered Hull: curva de dificuldade por setor.
//Multiplica a vida máxima e o dano corpo a corpo dos inimigos.
//Dentro de cada região a subida é suave; depois de cada chefe há um degrau.
//Os números partem da proposta do documento de design e precisam ser
//confirmados com dados de partidas reais (meta: 15 a 20% terminam sem continue).
public class HullBalance {

	//índice = setor; setores 5, 10, 15, 20 e 25 são de chefe
	private static final float[] CURVE = {
			1.00f,                                  //0 (não usado)
			0.85f, 0.88f, 0.91f, 0.94f, 0.97f,      //1-5   destroços da nave
			1.02f, 1.03f, 1.04f, 1.05f, 1.06f,      //6-10  planícies de cinza
			1.10f, 1.11f, 1.12f, 1.13f, 1.14f,      //11-15 cavernas profundas
			1.18f, 1.19f, 1.20f, 1.21f, 1.22f,      //16-20 colmeia exterior
			1.26f, 1.27f, 1.28f, 1.29f, 1.30f,      //21-25 câmara da rainha
			1.30f                                   //26
	};

	public static float depthModifier( int depth ){
		if (depth < 1) return 1f;
		if (depth >= CURVE.length) return CURVE[CURVE.length-1];
		return CURVE[depth];
	}

	public static float mobModifier( Char ch ){
		if (!(ch instanceof Mob) || ch instanceof NPC) return 1f;
		if (ch.alignment != Char.Alignment.ENEMY) return 1f;
		return depthModifier(Dungeon.depth);
	}

	//carregadores gerados por setor: 1 garantido e esta chance de um segundo
	public static final float SECOND_MAGAZINE_CHANCE = 0.5f;
}
