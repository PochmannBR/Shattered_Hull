/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dobrokhotov
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
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
 */

package com.shatteredpixel.shatteredpixeldungeon.items.hull;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: o pedido de suprimento do Exterminador, feito pelo transmissor orbital
public class PedidoSuprimento {

	public enum Tipo {
		ATAQUE(1, 1, true),
		MUNICAO_INCENDIARIA(1, 1, false),
		TORRETA_LEVE(1, 1, false),
		ESCUDO_CAMPO(1, 1, false),
		GRANADAS(2, 1, false),
		DRONE_ESCOLTA(2, 2, false),
		MUNICAO_CRIOGENICA(3, 1, false),
		TORRETA_PESADA(3, 2, false),
		MUNICAO_CHOQUE(4, 1, false),
		ESCUDO_REFORCADO(4, 2, false),
		ITEM_RARO(5, 3, false);

		public final int regiao, custo;
		//o ataque puro é exclusivo do Observador avançado
		public final boolean soObservador;

		Tipo(int regiao, int custo, boolean soObservador){
			this.regiao = regiao;
			this.custo = custo;
			this.soObservador = soObservador;
		}

		public String nome(){
			return Messages.get(PedidoSuprimento.class, "tipo_" + name().toLowerCase());
		}
	}

	public static boolean usa(Hero hero){
		return hero != null && hero.heroClass == HeroClass.MAGE;
	}

	//nível do lançador de torretas que a cápsula entrega. 0.9.1, decisão de Pablo em 22/11/2026: a leve
	//começa no nível 2, com 3 disparos, e passa ao nível 3, com 5 disparos e 35 de vida, a partir da região 3.
	//No nível 1 do jogo original o posto se desfazia depois de um único disparo.
	public static int nivelTorreta(Tipo tipo, int regiao, int embalagem){
		int tier = tipo == Tipo.TORRETA_LEVE ? (regiao >= 3 ? 3 : 2) : Math.min(6, 3 + regiao / 2);
		if (embalagem >= 2) tier = Math.min(6, tier + 1);
		return tier;
	}

	public static int regiaoAlcancada(){
		return Math.min(5, (Math.max(1, Math.max(Statistics.deepestFloor, Dungeon.depth)) - 1) / 5 + 1);
	}

	//até onde o catálogo vai, contando a requisição prioritária do Intendente
	public static int regiaoLiberada(Hero hero){
		int r = regiaoAlcancada();
		int p = hero.pointsInTalent(Talent.REQUISICAO_PRIORITARIA);
		if (p >= 1) r++;
		if (p >= 3) r++;
		return r;
	}

	public static boolean disponivel(Hero hero, Tipo t){
		if (t.soObservador) return hero.subClass == HeroSubClass.WARLOCK;
		return t.regiao <= regiaoLiberada(hero);
	}

	public static int custo(Hero hero, Tipo t){
		int c = t.custo;
		if (hero.subClass == HeroSubClass.BATTLEMAGE && c >= 2) c--;
		//itens de região à frente custam uma carga a mais, menos com a requisição prioritária completa
		int adiante = t.regiao - regiaoAlcancada();
		if (adiante > 0 && !t.soObservador){
			int p = hero.pointsInTalent(Talent.REQUISICAO_PRIORITARIA);
			if (adiante == 1 && p == 1) c++;
			if (adiante == 2) c++;
		}
		return c;
	}

	public static int alcance(Hero hero){
		return 6 + 2 * hero.pointsInTalent(Talent.ALCANCE_ORBITAL);
	}

	public static int espera(Hero hero){
		return Math.max(0, 2 - hero.pointsInTalent(Talent.CONTAGEM_CURTA));
	}

	//ajustado por Pablo em 22/11/2026, depois da simulação: a cápsula é um golpe de abertura, não um abate certo
	public static int dano(Hero hero){
		float d = 2 + hero.lvl;
		d *= 1f + 0.2f * hero.pointsInTalent(Talent.IMPACTO_REFORCADO);
		if (hero.subClass == HeroSubClass.WARLOCK) d *= 1.5f;
		return Math.round(d);
	}

	public static Cargas cargas(Hero hero){
		Cargas c = hero.buff(Cargas.class);
		if (c == null && usa(hero)) c = Buff.affect(hero, Cargas.class);
		return c;
	}

	//---------------------------------------------------------------- catálogo e alvo

	public static void abrirCatalogo(final Hero hero){
		final Cargas c = cargas(hero);
		if (c == null) return;
		final ArrayList<Tipo> lista = new ArrayList<>();
		ArrayList<String> nomes = new ArrayList<>();
		for (Tipo t : Tipo.values()){
			if (disponivel(hero, t)){
				lista.add(t);
				int custo = custo(hero, t);
				nomes.add(Messages.get(PedidoSuprimento.class, "opcao", t.nome(), custo));
			}
		}
		GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.MAGES_STAFF),
				Messages.get(PedidoSuprimento.class, "titulo"),
				Messages.get(PedidoSuprimento.class, "catalogo", (int)c.cargas, c.max()),
				nomes.toArray(new String[0])){
			@Override
			protected boolean enabled(int index) {
				return c.cargas >= custo(hero, lista.get(index));
			}
			@Override
			protected void onSelect(int index) {
				escolherAlvo(hero, lista.get(index));
			}
		});
	}

	public static void escolherAlvo(final Hero hero, final Tipo tipo){
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) return;
				if (!valido(hero, cell)){
					GLog.w(Messages.get(PedidoSuprimento.class, "fora_alcance"));
					return;
				}
				pedir(hero, tipo, cell);
			}
			@Override
			public String prompt() {
				return Messages.get(PedidoSuprimento.class, "prompt");
			}
		});
	}

	public static boolean valido(Hero hero, int cell){
		if (cell < 0 || cell >= Dungeon.level.length()) return false;
		if (Dungeon.level.solid[cell] || !Dungeon.level.passable[cell]) return false;
		if (!Dungeon.level.heroFOV[cell] && !Dungeon.level.mapped[cell] && !Dungeon.level.visited[cell]) return false;
		return Dungeon.level.distance(hero.pos, cell) <= alcance(hero);
	}

	//pedido já validado: gasta as cargas e marca a queda
	public static void pedir(Hero hero, Tipo tipo, int cell){
		Cargas c = cargas(hero);
		int custo = custo(hero, tipo);
		if (c == null || c.cargas < custo) return;
		c.cargas -= custo;
		agendar(hero, cell, tipo, false);
		//0.9.1: guarda o custo na queda, para devolver a carga se o herói trocar de setor antes dela
		for (Queda q : hero.buffs(Queda.class)) if (q.cell == cell && q.tipo == tipo && !q.extra && q.custo == 0) q.custo = custo;
		//cápsula dupla: chance de uma segunda cápsula, só de impacto, no mesmo ponto
		if (Random.Int(3) < hero.pointsInTalent(Talent.CAPSULA_DUPLA)){
			agendar(hero, cell, tipo, true);
		}
		Sample.INSTANCE.play(Assets.Sounds.BEACON);
		GLog.i(Messages.get(PedidoSuprimento.class, "pedido", tipo.nome()));
		if (hero.sprite != null) hero.sprite.zap(cell);
		hero.spendAndNext(Actor.TICK);
	}

	public static void agendar(Hero hero, int cell, Tipo tipo, boolean extra){
		int espera = espera(hero);
		if (espera <= 0){
			impacto(cell, tipo, extra);
		} else {
			Queda q = Buff.append(hero, Queda.class);
			q.cell = cell;
			q.tipo = tipo;
			q.turnos = espera;
			q.extra = extra;
			q.setor = Dungeon.depth;
			q.ramo = Dungeon.branch;
			GameScene.targetedCell(cell, 0xFF8800, espera);
		}
	}

	//---------------------------------------------------------------- queda e entrega

	public static void impacto(int cell, Tipo tipo, boolean extra){
		Hero hero = Dungeon.hero;
		if (hero == null || Dungeon.level == null) return;
		//a área de 3 por 3 do Observador vale só para o ataque orbital
		boolean area = hero.subClass == HeroSubClass.WARLOCK && tipo == Tipo.ATAQUE;
		int dano = dano(hero);
		if (tipo == Tipo.ATAQUE) dano *= 2;
		if (extra) dano = Math.max(1, dano / 2);

		Cargas c = hero.buff(Cargas.class);
		ArrayList<Integer> celulas = new ArrayList<>();
		celulas.add(cell);
		if (area){
			for (int n : PathFinder.NEIGHBOURS8){
				int p = cell + n;
				if (p >= 0 && p < Dungeon.level.length() && !Dungeon.level.solid[p]) celulas.add(p);
			}
		}

		for (int p : celulas){
			Char ch = Actor.findChar(p);
			if (ch != null && ch != hero && ch.alignment != Char.Alignment.ALLY){
				float d = dano;
				//correção de tiro: pedidos seguidos no mesmo alvo
				int pc = hero.pointsInTalent(Talent.CORRECAO_TIRO);
				if (c != null && pc > 0){
					if (c.ultimoAlvo == ch.id()) c.sequencia++;
					else { c.ultimoAlvo = ch.id(); c.sequencia = 0; }
					d *= 1f + 0.15f * pc * c.sequencia;
				}
				ch.damage(Math.round(d), PedidoSuprimento.class);
				int pf = hero.pointsInTalent(Talent.FOGO_SUPRESSAO);
				if (pf > 0 && ch.isAlive()) Buff.prolong(ch, Paralysis.class, pf);
			}
			int ps = hero.pointsInTalent(Talent.SATURACAO);
			if (ps > 0 && !Dungeon.level.water[p]){
				GameScene.add(Blob.seed(p, 2 + 2 * ps, Fire.class));
			}
			if (Dungeon.level.heroFOV[p]) CellEmitter.get(p).burst(SmokeParticle.FACTORY, 4);
		}

		if (Dungeon.level.heroFOV[cell]){
			CellEmitter.center(cell).burst(BlastParticle.FACTORY, 20);
			if (Camera.main != null) Camera.main.shake(2, 0.3f);
		}
		Sample.INSTANCE.play(Assets.Sounds.BLAST);

		if (extra || tipo == Tipo.ATAQUE) return;

		entregar(hero, cell, tipo);

		int pr = hero.pointsInTalent(Talent.RECICLAGEM_CAPSULA);
		if (pr > 0){
			soltar(new Gold(5 * regiaoAlcancada() * pr), cell);
		}
	}

	private static int livre(int cell){
		if (Actor.findChar(cell) == null && Dungeon.level.passable[cell]) return cell;
		ArrayList<Integer> op = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int p = cell + n;
			if (Actor.findChar(p) == null && Dungeon.level.passable[p]) op.add(p);
		}
		return op.isEmpty() ? -1 : Random.element(op);
	}

	private static void soltar(Item item, int cell){
		com.shatteredpixel.shatteredpixeldungeon.items.Heap h = Dungeon.level.drop(item, cell);
		if (h.sprite != null) h.sprite.drop();
	}

	public static void entregar(Hero hero, int cell, Tipo tipo){
		int emb = hero.pointsInTalent(Talent.EMBALAGEM_REFORCADA);
		int r = regiaoAlcancada();
		switch (tipo){
			case MUNICAO_INCENDIARIA:
				soltar(new MunicaoEspecial.Incendiaria().quantity(emb >= 1 ? 2 : 1), cell);
				break;
			case MUNICAO_CRIOGENICA:
				soltar(new MunicaoEspecial.Criogenica().quantity(emb >= 1 ? 2 : 1), cell);
				break;
			case MUNICAO_CHOQUE:
				soltar(new MunicaoEspecial.Choque().quantity(emb >= 1 ? 2 : 1), cell);
				break;
			case GRANADAS:
				soltar(new Bomb().quantity(emb >= 1 ? 3 : 2), cell);
				break;
			case ESCUDO_CAMPO:
				soltar(new CelulaEscudo().forca(emb >= 2 ? 2 : 1), cell);
				break;
			case ESCUDO_REFORCADO:
				soltar(new CelulaEscudo().forca(emb >= 2 ? 3 : 2), cell);
				break;
			case TORRETA_LEVE: case TORRETA_PESADA: {
				int p = livre(cell);
				if (p == -1) break;
				int tier = nivelTorreta(tipo, r, emb);
				WandOfWarding.Ward ward = new WandOfWarding.Ward();
				ward.pos = p;
				ward.configurar(tier, 1 + hero.lvl / 3);
				int pt = hero.pointsInTalent(Talent.TORRETA_REFORCADA);
				if (pt > 0 && tier >= 3){
					ward.HT = Math.round(ward.HT * (1f + 0.25f * pt));
					ward.HP = ward.HT;
				}
				GameScene.add(ward, 1f);
				Dungeon.level.occupyCell(ward);
				break;
			}
			case DRONE_ESCOLTA: {
				int p = livre(cell);
				if (p == -1) break;
				MirrorImage drone = new MirrorImage();
				drone.duplicate(hero);
				GameScene.add(drone);
				ScrollOfTeleportation.appear(drone, p);
				Buff.affect(drone, Temporario.class, 30f);
				break;
			}
			case ITEM_RARO: {
				Item i = Random.Int(2) == 0 ? Generator.random(Generator.Category.WAND) : Generator.random(Generator.Category.RING);
				soltar(i, cell);
				break;
			}
			default:
				break;
		}
	}

	//---------------------------------------------------------------- efeitos

	//aliado que some quando o tempo acaba, como o drone de escolta
	public static class Temporario extends FlavourBuff {
		@Override
		public void detach() {
			super.detach();
			if (target != null && target.isAlive()) target.die(null);
		}
	}

	public static class Queda extends Buff {
		{
			actPriority = HERO_PRIO + 1;
		}
		public int cell;
		public Tipo tipo;
		public int turnos;
		public boolean extra;
		//0.9.1: setor em que a queda foi marcada; a cápsula não cai em outro setor
		public int setor = -1, ramo = 0, custo = 0;

		@Override
		public boolean act() {
			if (setor >= 0 && (setor != Dungeon.depth || ramo != Dungeon.branch)){
				//o herói saiu do setor antes da queda: o pedido é cancelado e a carga volta
				Cargas c = target instanceof Hero ? ((Hero) target).buff(Cargas.class) : null;
				if (c != null && custo > 0) c.ganhar(custo);
				detach();
				return true;
			}
			turnos--;
			if (turnos <= 0){
				impacto(cell, tipo, extra);
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}

		private static final String CELL = "cell", TIPO = "tipo", TURNOS = "turnos", EXTRA = "extra", SETOR = "setor", RAMO = "ramo", CUSTO = "custo";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CELL, cell);
			bundle.put(TIPO, tipo);
			bundle.put(TURNOS, turnos);
			bundle.put(EXTRA, extra);
			bundle.put(SETOR, setor);
			bundle.put(RAMO, ramo);
			bundle.put(CUSTO, custo);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			cell = bundle.getInt(CELL);
			tipo = bundle.getEnum(TIPO, Tipo.class);
			turnos = bundle.getInt(TURNOS);
			extra = bundle.getBoolean(EXTRA);
			setor = bundle.contains(SETOR) ? bundle.getInt(SETOR) : -1;
			ramo = bundle.getInt(RAMO);
			custo = bundle.getInt(CUSTO);
		}
	}

	//cargas do pedido, guardadas no herói
	public static class Cargas extends Buff {
		{
			type = buffType.POSITIVE;
			revivePersists = true;
		}

		public float cargas = 3;
		public float parcial = 0;
		public int ultimoAlvo = -1;
		public int sequencia = 0;
		//sinal de emergência: quantas vezes já foi usado no setor atual
		public int sinais = 0;
		public int setorSinais = -1;

		public int max(){
			Hero h = (Hero)target;
			return 3 + (h == null ? 0 : h.pointsInTalent(Talent.ESTOQUE));
		}

		public float tempoRecarga(){
			Hero h = (Hero)target;
			float t = 100f;
			if (h.subClass == HeroSubClass.BATTLEMAGE) t *= 0.75f;
			t *= 1f - 0.15f * h.pointsInTalent(Talent.RECARGA_SATELITE);
			return t;
		}

		public void ganhar(float quanto){
			cargas = Math.min(max(), cargas + quanto);
		}

		@Override
		public boolean act() {
			if (target instanceof Hero && cargas < max()){
				//parcial conta turnos, para não acumular erro de arredondamento
				parcial += 1f;
				if (parcial >= tempoRecarga()){
					parcial = 0;
					cargas++;
				}
				if (cargas >= max()) parcial = 0;
			}
			spend(TICK);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.RECHARGING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.55f, 0.1f);
		}

		@Override
		public float iconFadePercent() {
			return cargas >= max() ? 0 : 1f - parcial / tempoRecarga();
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)cargas);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)cargas, max(), Math.round(tempoRecarga()));
		}

		private static final String CARGAS = "cargas", PARCIAL = "parcial", ULTIMO = "ultimo", SEQ = "seq", SINAIS = "sinais", SETOR = "setor";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CARGAS, cargas);
			bundle.put(PARCIAL, parcial);
			bundle.put(ULTIMO, ultimoAlvo);
			bundle.put(SEQ, sequencia);
			bundle.put(SINAIS, sinais);
			bundle.put(SETOR, setorSinais);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			cargas = bundle.getFloat(CARGAS);
			parcial = bundle.getFloat(PARCIAL);
			ultimoAlvo = bundle.getInt(ULTIMO);
			sequencia = bundle.getInt(SEQ);
			sinais = bundle.getInt(SINAIS);
			setorSinais = bundle.getInt(SETOR);
		}
	}

	//sinal de emergência: com a vida abaixo de 30%, uma carga na hora, uma ou duas vezes por setor
	public static void sinalEmergencia(Hero hero){
		int p = hero.pointsInTalent(Talent.SINAL_EMERGENCIA);
		if (p == 0 || !hero.isAlive() || hero.HP >= hero.HT * 0.3f) return;
		Cargas c = cargas(hero);
		if (c == null) return;
		if (c.setorSinais != Dungeon.depth){
			c.setorSinais = Dungeon.depth;
			c.sinais = 0;
		}
		if (c.sinais < p && c.cargas < c.max()){
			c.sinais++;
			c.ganhar(1);
			GLog.p(Messages.get(PedidoSuprimento.class, "sinal"));
		}
	}

	//refeição de campanha: comer recupera carga do transmissor
	public static void refeicao(Hero hero){
		int p = hero.pointsInTalent(Talent.REFEICAO_CAMPANHA);
		if (p == 0) return;
		Cargas c = cargas(hero);
		if (c != null) c.ganhar(p * 0.5f);
	}
}
