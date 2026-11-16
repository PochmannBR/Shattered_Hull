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
//Na 0.7 a curva foi achatada a partir do simulador de balanceamento (testes/Balanco.java):
//o fuzil dá muita vantagem no começo e perde peso no fim, então a curva sobe no
//começo e fica perto de 1,00 nas últimas regiões. Os próprios inimigos já ficam
//mais fortes a cada região. Meta: 15 a 20% das missões terminam sem continue.
public class HullBalance {

	//índice = setor; setores 5, 10, 15, 20 e 25 são de chefe
	private static final float[] CURVE = {
			1.00f,                                  //0 (não usado)
			0.95f, 0.98f, 1.01f, 1.04f, 1.06f,      //1-5   destroços da nave
			1.06f, 1.07f, 1.08f, 1.09f, 1.10f,      //6-10  planícies de cinza
			1.02f, 1.03f, 1.04f, 1.05f, 1.06f,      //11-15 cavernas profundas
			1.00f, 1.01f, 1.02f, 1.03f, 1.04f,      //16-20 colmeia exterior
			0.99f, 1.00f, 1.01f, 1.02f, 1.03f,      //21-25 câmara da rainha
			1.03f                                   //26
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
