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

//Shattered Hull: regras do continue pago.
//Um por missão, só a partir do setor 6. O jogador volta no início do setor
//com metade da vida e sem recuperar munição, remédios ou itens.
//A missão salva pelo continue fica marcada no ranking.
public class HullContinue {

	public static final int MIN_DEPTH = 6;

	//causa usada quando o jogo é reaberto com a janela do continue pendente
	public static class UnknownCause {}

	public interface Listener {
		void onResult( boolean granted );
	}

	public static boolean canOffer(){
		return !Statistics.continueUsed
				&& Dungeon.depth >= MIN_DEPTH
				&& !Dungeon.daily;
	}

	//Ponto de integração com a loja do Android/iOS. Por enquanto o continue
	//é concedido direto, para testar o fluxo. Quando a compra existir, chamar
	//listener.onResult(true) só depois da confirmação do pagamento.
	public static void requestPurchase( Listener listener ){
		listener.onResult( true );
	}
}
