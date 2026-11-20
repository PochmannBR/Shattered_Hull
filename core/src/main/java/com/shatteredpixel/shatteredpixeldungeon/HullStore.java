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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.GameSettings;

import java.util.EnumSet;

//Shattered Hull: compras dentro do jogo.
//O núcleo só conhece os produtos, o que o jogador já tem e a regra do teste
//das classes. Quem fala com a loja de verdade é a implementação de cada
//plataforma, colocada em HullStore.instance pelo launcher.
public abstract class HullStore {

	public enum Produto {
		CONTINUE            ( "continue_partida",        true,  null ),
		APOIADOR            ( "apoiador",                false, null ),
		PACOTE_CLASSES      ( "pacote_classes",          false, null ),
		CLASSE_EXTERMINADOR ( "classe_exterminador",     false, HeroClass.MAGE ),
		CLASSE_FORCAS       ( "classe_forcas_especiais", false, HeroClass.ROGUE ),
		CLASSE_PILOTO       ( "classe_piloto",           false, HeroClass.HUNTRESS ),
		CLASSE_RENEGADO     ( "classe_renegado",         false, HeroClass.DUELIST ),
		CLASSE_SINTETICO    ( "classe_sintetico",        false, HeroClass.CLERIC );

		//identificador do produto no Google Play Console
		public final String id;
		//o continue é consumível; os outros são comprados uma vez só
		public final boolean consumivel;
		public final HeroClass classe;

		Produto( String id, boolean consumivel, HeroClass classe ){
			this.id = id;
			this.consumivel = consumivel;
			this.classe = classe;
		}

		public static Produto porId( String id ){
			for (Produto p : values()) if (p.id.equals(id)) return p;
			return null;
		}

		public static Produto daClasse( HeroClass cl ){
			for (Produto p : values()) if (p.classe == cl) return p;
			return null;
		}
	}

	public enum Resultado { COMPRADO, CANCELADO, PENDENTE, ERRO, INDISPONIVEL }

	public interface Callback1 {
		void resultado( Resultado r );
	}

	//Classes com conteúdo pronto. Uma classe só pode ser comprada ou testada
	//depois de entrar aqui; até lá ela aparece na loja como "em breve".
	public static final EnumSet<HeroClass> CLASSES_PRONTAS = EnumSet.of(HeroClass.WARRIOR, HeroClass.ROGUE, HeroClass.MAGE, HeroClass.CLERIC);

	//O teste gratuito de uma classe vai até o fim deste setor.
	public static final int TESTE_ATE_SETOR = 5;

	public static HullStore instance = new Simulada();

	// ---------------- o que o jogador já tem ----------------

	private static final String PREFIXO = "hull_compra_";

	public static boolean possui( Produto p ){
		if (p.consumivel) return false;
		return GameSettings.getBoolean(PREFIXO + p.id, false);
	}

	//chamado pela plataforma depois de confirmar ou restaurar uma compra
	public static void registrar( Produto p, boolean possui ){
		if (p.consumivel) return;
		GameSettings.put(PREFIXO + p.id, possui);
	}

	//continues pagos que não chegaram a ser usados
	private static final String CREDITO = "hull_credito_continue";

	public static int creditosContinue(){
		return GameSettings.getInt(CREDITO, 0);
	}

	public static void adicionarCreditoContinue(){
		GameSettings.put(CREDITO, creditosContinue() + 1);
	}

	public static boolean usarCreditoContinue(){
		int c = creditosContinue();
		if (c <= 0) return false;
		GameSettings.put(CREDITO, c - 1);
		return true;
	}

	public static boolean apoiador(){
		return possui(Produto.APOIADOR);
	}

	//o Fuzileiro vem com o jogo; as outras classes vêm pela compra própria ou pelo pacote
	public static boolean possuiClasse( HeroClass cl ){
		if (cl == HeroClass.WARRIOR) return true;
		Produto p = Produto.daClasse(cl);
		return possui(Produto.PACOTE_CLASSES) || (p != null && possui(p));
	}

	public static boolean classePronta( HeroClass cl ){
		return CLASSES_PRONTAS.contains(cl);
	}

	//pode começar uma missão com a classe, comprada ou em teste
	public static boolean podeJogar( HeroClass cl ){
		return classePronta(cl);
	}

	public static boolean emTeste( HeroClass cl ){
		return classePronta(cl) && !possuiClasse(cl);
	}

	//a missão atual passou do limite do teste
	public static boolean testeEsgotado( HeroClass cl, int setor ){
		return emTeste(cl) && setor > TESTE_ATE_SETOR;
	}

	//o produto pode ser oferecido na loja agora
	public static boolean aVenda( Produto p ){
		if (p.classe != null) return classePronta(p.classe) && !possuiClasse(p.classe);
		if (p == Produto.PACOTE_CLASSES){
			for (HeroClass cl : HeroClass.values()){
				if (cl != HeroClass.WARRIOR && classePronta(cl) && !possuiClasse(cl)) return true;
			}
			return false;
		}
		return !possui(p);
	}

	//entrega o resultado na thread do jogo, de onde quer que a loja responda
	protected static void entregar( final Callback1 cb, final Resultado r ){
		if (cb == null) return;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				cb.resultado(r);
			}
		});
	}

	// ---------------- o que cada plataforma implementa ----------------

	//a loja está conectada e pode vender
	public abstract boolean disponivel();

	//preço formatado na moeda do jogador, ou null se ainda não carregou
	public abstract String preco( Produto p );

	public abstract void comprar( Produto p, Callback1 cb );

	//consulta as compras já feitas na conta e atualiza o que o jogador tem
	public abstract void restaurar( Callback1 cb );

	//a compra é simulada, sem cobrança; usada no desktop e nas builds de teste
	public boolean simulada(){
		return false;
	}

	// ---------------- loja simulada ----------------

	public static class Simulada extends HullStore {

		@Override
		public boolean disponivel() {
			return true;
		}

		@Override
		public String preco( Produto p ) {
			switch (p){
				case CONTINUE: return "US$ 1,99";
				default: return "US$ 0,00 (teste)";
			}
		}

		@Override
		public void comprar( Produto p, Callback1 cb ) {
			registrar(p, true);
			entregar(cb, Resultado.COMPRADO);
		}

		@Override
		public void restaurar( Callback1 cb ) {
			entregar(cb, Resultado.COMPRADO);
		}

		@Override
		public boolean simulada() {
			return true;
		}
	}
}
