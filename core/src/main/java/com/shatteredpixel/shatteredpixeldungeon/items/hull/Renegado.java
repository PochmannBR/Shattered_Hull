/*
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 *
 * Baseado em Shattered Pixel Dungeon, Copyright (C) 2014-2025 Evan Debenham,
 * e Pixel Dungeon, Copyright (C) 2012-2015 Oleg Dobrokhotov.
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
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: o Renegado. Medidor de assimilação no lugar das cargas da Duelista, amostras do
//enxame em quatro lugares do corpo, surto e mutações.
public class Renegado {

	public enum Lugar { CABECA, BRACOS, TRONCO, PERNAS }

	public enum Amostra {
		MANDIBULAS(Lugar.CABECA, 10, Rat.class),
		PROBOSCIDE(Lugar.CABECA, 15, Bat.class),
		COMANDO(Lugar.CABECA, 25, Necromancer.class),
		GARRAS(Lugar.BRACOS, 10, Albino.class),
		MUCO(Lugar.BRACOS, 15, CausticSlime.class),
		PINCA(Lugar.BRACOS, 10, Crab.class),
		PLACA(Lugar.TRONCO, 15, HermitCrab.class),
		GELATINOSO(Lugar.TRONCO, 15, Slime.class),
		ESTILHACOS(Lugar.TRONCO, 20, Skeleton.class),
		BOTE(Lugar.PERNAS, 20, Snake.class),
		TUNEL(Lugar.PERNAS, 20, Crab.class),
		SALTO(Lugar.PERNAS, 15, PhantomPiranha.class),
		//criaturas mais fortes, das regiões 3 a 5; a classe tem de ser exata, porque o lança-ferrões
		//ácido herda do lança-ferrões e o mantídeo veterano herda do mantídeo
		TEIA(Lugar.CABECA, 15, Spinner.class, true),
		OLHAR(Lugar.CABECA, 25, Eye.class, true),
		ENCANTO(Lugar.CABECA, 25, Succubus.class, true),
		FOICES(Lugar.BRACOS, 15, Monk.class, true),
		FERRAO(Lugar.BRACOS, 15, Scorpio.class, true),
		FERRAO_ACIDO(Lugar.BRACOS, 20, Acidic.class, true),
		SEGUNDA_CHANCE(Lugar.TRONCO, 25, Ghoul.class, true),
		CARAPACA(Lugar.TRONCO, 20, Golem.class, true),
		COMBUSTAO(Lugar.TRONCO, 20, Elemental.FireElemental.class, true),
		SALTO_ESTRIPADOR(Lugar.PERNAS, 20, RipperDemon.class, true),
		FOCO(Lugar.PERNAS, 10, Senior.class, true),
		RASTRO_GELIDO(Lugar.PERNAS, 15, Elemental.FrostElemental.class, true);

		public final Lugar lugar;
		public final int medidor;
		public final Class<? extends Mob> fonte;
		public final boolean exata;

		Amostra(Lugar lugar, int medidor, Class<? extends Mob> fonte){
			this(lugar, medidor, fonte, false);
		}

		Amostra(Lugar lugar, int medidor, Class<? extends Mob> fonte, boolean exata){
			this.lugar = lugar;
			this.medidor = medidor;
			this.fonte = fonte;
			this.exata = exata;
		}

		public boolean de(Mob m){
			return exata ? m.getClass() == fonte : fonte.isInstance(m);
		}

		public String nome(){
			return Messages.get(Renegado.class, "amostra_" + name().toLowerCase());
		}

		public String habilidade(){
			return Messages.get(Renegado.class, "hab_" + name().toLowerCase());
		}

		public boolean comAlvo(){
			switch (this){
				case PLACA: case GELATINOSO: case ESTILHACOS: case COMANDO:
				case SEGUNDA_CHANCE: case CARAPACA: case COMBUSTAO: case FOCO: case RASTRO_GELIDO:
					return false;
				default:
					return true;
			}
		}
	}

	public static final int VAZIO = -1, TECIDO = -2;

	public static boolean e(Char c){
		return c instanceof Hero && ((Hero) c).heroClass == HeroClass.DUELIST;
	}

	public static Medidor medidor(Hero h){
		if (!e(h)) return null;
		Medidor m = h.buff(Medidor.class);
		if (m == null) m = Buff.affect(h, Medidor.class);
		return m;
	}

	//consulta sem criar o buff, para não entrar em laço com as imunidades
	private static Medidor ver(Hero h){
		return h == null ? null : h.buff(Medidor.class);
	}

	//tolerância: o medidor enche em 100, mas o surto só vem ao passar de 100 mais 3 por nível acima do primeiro
	public static final int TOLERANCIA_POR_NIVEL = 3;

	public static int limite(Hero h){
		return 100 + TOLERANCIA_POR_NIVEL * Math.max(0, h.lvl - 1);
	}

	public static float valor(Hero h){
		Medidor m = ver(h);
		return m == null ? 0 : m.valor;
	}

	//---------------------------------------------------------------- enxame e assimilados

	public static boolean assimilado(Char ch){
		return ch instanceof Gnoll || ch instanceof GnollGuard || ch instanceof GnollSapper || ch instanceof GnollGeomancer
				|| ch instanceof GnollExile || ch instanceof Brute || ch instanceof Shaman
				|| ch.getClass().getSimpleName().startsWith("GnollTrickster");
	}

	public static boolean enxame(Char ch){
		if (!(ch instanceof Mob) || ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC) return false;
		if (assimilado(ch) || Char.hasProp(ch, Char.Property.INORGANIC)) return false;
		if (ch instanceof Thief || ch instanceof Mimic || ch instanceof Statue || ch instanceof Wraith) return false;
		return ch.alignment == Char.Alignment.ENEMY;
	}

	public static Amostra amostraDe(Mob m){
		ArrayList<Amostra> op = new ArrayList<>();
		for (Amostra a : Amostra.values()) if (a.de(m)) op.add(a);
		return op.isEmpty() ? null : Random.element(op);
	}

	//---------------------------------------------------------------- faixas do medidor

	public static int inicioFaixaAlta(Hero h){
		return 75 - 5 * h.pointsInTalent(Talent.FAIXA_ALTA);
	}

	public static float fatorDano(Hero h){
		if (!e(h)) return 1f;
		float v = valor(h);
		float f = 1f;
		if (v >= inicioFaixaAlta(h)) f = 1f + 0.25f * (h.subClass == HeroSubClass.CHAMPION ? 1.5f : 1f);
		else if (v >= 50) f = 1.1f;
		else if (h.subClass == HeroSubClass.MONK) f = 1.1f;
		if (h.buff(Surto.class) != null) f *= 1.3f;
		return f;
	}

	public static float fatorPrecisao(Hero h){
		if (!e(h)) return 1f;
		float f = 1f;
		if (h.subClass == HeroSubClass.MONK && valor(h) < 50) f *= 1.1f;
		Medidor m = ver(h);
		if (m != null && m.mutado(Lugar.BRACOS)) f *= 1f - 0.1f * fatorDefeito(h);
		return f;
	}

	public static float fatorVelocidade(Hero h){
		if (!e(h)) return 1f;
		float f = 1f;
		Medidor m = ver(h);
		if (m != null && m.mutado(Lugar.PERNAS)) f *= 1f - 0.1f * fatorDefeito(h);
		if (h.buff(Surto.class) != null) f *= 1.5f;
		return f;
	}

	//cura de injetores fica mais fraca a partir de 50
	public static float fatorCura(Char c){
		if (!e(c)) return 1f;
		return valor((Hero) c) >= 50 ? 0.75f : 1f;
	}

	public static float fatorDefeito(Hero h){
		return Math.max(0, 1f - 0.25f * h.pointsInTalent(Talent.MUTACAO_ADAPTADA));
	}

	//enxame hesita em atacar acima da faixa alta; o foco do mantídeo veterano desvia um ataque qualquer
	public static boolean hesita(Char atacante, Char defensor){
		if (!e(defensor) || atacante == defensor) return false;
		Foco foco = defensor.buff(Foco.class);
		if (foco != null){
			foco.detach();
			return true;
		}
		if (!enxame(atacante)) return false;
		return valor((Hero) defensor) >= inicioFaixaAlta((Hero) defensor) && Random.Int(8) == 0;
	}

	public static boolean imune(Char c, Class efeito){
		if (!e(c)) return false;
		Medidor m = ver((Hero) c);
		//a feromante só funciona nele depois de mutar a cabeça
		return Charm.class.isAssignableFrom(efeito) && (m == null || !m.mutado(Lugar.CABECA));
	}

	public static int danoRecebido(Hero h, int dmg, Object src){
		if (!e(h) || dmg <= 0) return dmg;
		Medidor m = ver(h);
		if (m != null && m.mutado(Lugar.TRONCO) && (src instanceof Burning || src instanceof Fire)){
			dmg = Math.round(dmg * (1f + 0.5f * fatorDefeito(h)));
		}
		if (h.buff(Gelatinoso.class) != null && dmg > 6){
			dmg = 6 + (dmg - 6) / 2;
		}
		Carapaca cp = h.buff(Carapaca.class);
		if (cp != null && src instanceof Char){
			dmg = Math.max(0, dmg - Math.round(Random.NormalIntRange(1 + h.lvl / 4, 3 + h.lvl / 2) * cp.potencia));
		}
		SegundaChance sc = h.buff(SegundaChance.class);
		if (sc != null && dmg >= h.HP + h.shielding()){
			sc.detach();
			h.HP = Math.max(h.HP, Math.round(h.HT * 0.2f * sc.potencia));
			if (h.sprite != null) h.sprite.emitter().burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle.UP, 8);
			GLog.p(Messages.get(Renegado.class, "segunda_chance"));
			return 0;
		}
		//ácido e esporos alimentam o tecido do enxame: o medidor sobe um pouco
		if (m != null && acidoOuEsporo(src)){
			m.subir(Math.min(3, 1 + dmg / 5));
		}
		return dmg;
	}

	public static boolean acidoOuEsporo(Object src){
		return src instanceof Ooze
				|| src instanceof com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion
				|| src instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas
				|| src instanceof Elemental
				|| src instanceof FungalSentry || src instanceof FungalCore || src instanceof FungalSpinner;
	}

	public static boolean podeUsarItens(Hero h){
		if (h != null && h.buff(Surto.class) != null){
			GLog.w(Messages.get(Renegado.class, "surto_itens"));
			return false;
		}
		return true;
	}

	//---------------------------------------------------------------- abates e comida

	public static void abate(Mob m, Object causa){
		Hero h = Dungeon.hero;
		if (!e(h) || m.alignment != Char.Alignment.ENEMY) return;
		Medidor md = medidor(h);
		if (Char.hasProp(m, Char.Property.BOSS)){
			md.contagem = 0;
			GLog.p(Messages.get(Renegado.class, "contagem_zerada"));
		}
		if (causa == h && h.buff(AssimilacaoGarantida.class) != null){
			h.buff(AssimilacaoGarantida.class).detach();
			Amostra a = amostraDe(m);
			if (a == null && Random.Int(4) < h.pointsInTalent(Talent.COLETA_FORCADA)) a = Random.element(Amostra.values());
			if (a != null) soltarAmostra(a, m.pos);
			return;
		}
		Amostra a = amostraDe(m);
		if (a == null) return;
		float chance = 0.07f + 0.03f * h.pointsInTalent(Talent.COLETA_CUIDADOSA);
		if (m.buff(ChampionEnemy.class) != null) chance *= 3;
		if (Random.Float() < chance) soltarAmostra(a, m.pos);
	}

	private static void soltarAmostra(Amostra a, int pos){
		com.shatteredpixel.shatteredpixeldungeon.items.Heap heap = Dungeon.level.drop(new AmostraItem().tipo(a), pos);
		if (heap.sprite != null) heap.sprite.drop();
	}

	public static void comeu(Hero h, com.shatteredpixel.shatteredpixeldungeon.items.Item comida){
		if (!e(h)) return;
		if (comida instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat
				|| comida instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat
				|| comida instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio){
			int pf = h.pointsInTalent(Talent.FOME_ENXAME);
			if (pf > 0) h.HP = Math.min(h.HT, h.HP + Math.round(h.HT * 0.1f * pf));
			medidor(h).subir(10);
		}
	}

	//---------------------------------------------------------------- habilidades emprestadas

	public static int custo(Hero h, Amostra a){
		Medidor m = ver(h);
		if (h.buff(FormaEnxame.class) != null) return 0;
		if (m != null && h.subClass == HeroSubClass.MONK){
			if (m.setorCalma != Dungeon.depth){
				m.setorCalma = Dungeon.depth;
				m.calmasUsadas = 0;
			}
			if (m.calmasUsadas < h.pointsInTalent(Talent.CALMA_COMBATE)) return 0;
		}
		return Math.round(a.medidor * (1f - 0.15f * h.pointsInTalent(Talent.USO_ECONOMICO)));
	}

	public static float potencia(Hero h, Lugar l){
		Medidor m = ver(h);
		if (m == null || !m.mutado(l)) return 1f;
		return 1.5f + 0.25f * h.pointsInTalent(Talent.MUTACAO_PODEROSA);
	}

	public static void usar(final Hero h, final Amostra a){
		if (h.buff(Surto.class) != null){
			GLog.w(Messages.get(Renegado.class, "surto_itens"));
			return;
		}
		if (!a.comAlvo()){
			executar(h, a, -1);
			return;
		}
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell != null) executar(h, a, cell);
			}
			@Override
			public String prompt() {
				return Messages.get(Renegado.class, "prompt_" + a.name().toLowerCase());
			}
		});
	}

	private static Char inimigoAdjacente(Hero h, int cell){
		Char ch = Actor.findChar(cell);
		if (ch == null || ch == h || ch.alignment == Char.Alignment.ALLY || !Dungeon.level.adjacent(h.pos, cell)){
			GLog.w(Messages.get(Renegado.class, "sem_alvo_adjacente"));
			return null;
		}
		return ch;
	}

	private static void concluir(Hero h, Amostra a){
		Medidor m = medidor(h);
		int c = custo(h, a);
		if (c == 0 && h.subClass == HeroSubClass.MONK && h.buff(FormaEnxame.class) == null) m.calmasUsadas++;
		Invisibility.dispel();
		m.subir(c);
	}

	private static void golpe(final Hero h, final Amostra a, final Char alvo, final float mult, final float bonus, final Callback depois){
		h.busy();
		Callback golpe = new Callback() {
			@Override
			public void call() {
				int antes = alvo.HP;
				boolean acertou = h.attack(alvo, mult, bonus, Char.INFINITE_ACCURACY);
				if (acertou && depois != null){
					ultimoDano = Math.max(0, antes - alvo.HP);
					depois.call();
				}
				concluir(h, a);
				h.spendAndNext(h.attackDelay());
			}
		};
		if (h.sprite != null) h.sprite.attack(alvo.pos, golpe);
		else golpe.call();
	}

	private static int ultimoDano = 0;

	public static void executar(final Hero h, final Amostra a, int cell){
		final float p = potencia(h, a.lugar);
		switch (a){
			case MANDIBULAS: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, alvo.drRoll() * p, null);
				break;
			}
			case PROBOSCIDE: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, 0, new Callback() {
					@Override
					public void call() {
						int cura = Math.round(ultimoDano * 0.3f * p);
						if (cura > 0) h.HP = Math.min(h.HT, h.HP + cura);
					}
				});
				break;
			}
			case GARRAS: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, 0, new Callback() {
					@Override
					public void call() {
						if (alvo.isAlive()) Buff.affect(alvo, Bleeding.class).set((2 + h.lvl / 2f) * p);
					}
				});
				break;
			}
			case MUCO: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, 0, new Callback() {
					@Override
					public void call() {
						if (alvo.isAlive()) Buff.affect(alvo, Ooze.class).set(Ooze.DURATION * p);
					}
				});
				break;
			}
			case PINCA: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, 0, new Callback() {
					@Override
					public void call() {
						if (alvo.isAlive() && !Char.hasProp(alvo, Char.Property.IMMOVABLE)){
							int dir = alvo.pos - h.pos;
							Ballistica traj = new Ballistica(alvo.pos, alvo.pos + dir * 4, Ballistica.MAGIC_BOLT);
							WandOfBlastWave.throwChar(alvo, traj, Math.round(2 * p), true, true, h);
						}
					}
				});
				break;
			}
			case PLACA:
				Buff.affect(h, Barrier.class).incShield(Math.round(h.HT * 0.2f * p));
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			case GELATINOSO:
				Buff.affect(h, Gelatinoso.class, 10f * p);
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			case ESTILHACOS: {
				int dano = Math.round(Random.NormalIntRange(4 + h.lvl / 2, 8 + h.lvl) * p);
				for (int n : PathFinder.NEIGHBOURS8){
					Char ch = Actor.findChar(h.pos + n);
					if (ch != null && ch.alignment != Char.Alignment.ALLY) ch.damage(dano, h);
				}
				if (Dungeon.level.heroFOV[h.pos]) CellEmitter.center(h.pos).burst(BlastParticle.FACTORY, 12);
				Sample.INSTANCE.play(Assets.Sounds.BLAST);
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			}
			case COMANDO: {
				ArrayList<Integer> livres = new ArrayList<>();
				for (int n : PathFinder.NEIGHBOURS8){
					int c = h.pos + n;
					if (Dungeon.level.passable[c] && Actor.findChar(c) == null) livres.add(c);
				}
				if (livres.isEmpty()){
					GLog.w(Messages.get(Renegado.class, "sem_espaco"));
					return;
				}
				Skeleton casca = new Skeleton();
				casca.alignment = Char.Alignment.ALLY;
				casca.state = casca.HUNTING;
				GameScene.add(casca);
				ScrollOfTeleportation.appear(casca, Random.element(livres));
				Buff.affect(casca, PedidoSuprimento.Temporario.class, 15f * p);
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			}
			case BOTE: {
				Char alvo = Actor.findChar(cell);
				if (alvo == null || alvo.alignment == Char.Alignment.ALLY || alvo == h || !Dungeon.level.heroFOV[cell]
						|| Dungeon.level.distance(h.pos, cell) > 4){
					GLog.w(Messages.get(Renegado.class, "sem_alvo"));
					return;
				}
				int destino = celulaAoLado(h, cell);
				if (destino == -1){
					GLog.w(Messages.get(Renegado.class, "sem_espaco"));
					return;
				}
				boolean desprevenido = !(alvo instanceof Mob) || ((Mob) alvo).state != ((Mob) alvo).HUNTING;
				if (destino != h.pos) ScrollOfTeleportation.appear(h, destino);
				Dungeon.level.occupyCell(h);
				golpe(h, a, alvo, desprevenido ? 2f * p : p, 0, null);
				break;
			}
			case TUNEL: {
				Char alvo = Actor.findChar(cell);
				if (alvo == null || alvo.alignment == Char.Alignment.ALLY || alvo == h || !Dungeon.level.heroFOV[cell]
						|| Dungeon.level.distance(h.pos, cell) > Math.round(8 * p)){
					GLog.w(Messages.get(Renegado.class, "sem_alvo"));
					return;
				}
				int destino = celulaAoLado(h, cell);
				if (destino == -1){
					GLog.w(Messages.get(Renegado.class, "sem_espaco"));
					return;
				}
				ScrollOfTeleportation.appear(h, destino);
				Dungeon.level.occupyCell(h);
				Dungeon.observe();
				GameScene.updateFog();
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			}
			case SALTO: {
				if (!Dungeon.level.passable[cell] || Actor.findChar(cell) != null || !Dungeon.level.heroFOV[cell]
						|| Dungeon.level.distance(h.pos, cell) > Math.round(4 * p)){
					GLog.w(Messages.get(Renegado.class, "sem_espaco"));
					return;
				}
				ScrollOfTeleportation.appear(h, cell);
				Dungeon.level.occupyCell(h);
				Dungeon.observe();
				GameScene.updateFog();
				concluir(h, a);
				h.spendAndNext(Actor.TICK);
				break;
			}
			default:
				executarNova(h, a, cell, p);
				break;
		}
	}

	//inimigo visível ao alcance, com caminho livre para um projétil
	private static Char alvoADistancia(Hero h, int cell, int alcance, boolean projetil){
		Char ch = Actor.findChar(cell);
		if (ch == null || ch == h || ch.alignment == Char.Alignment.ALLY || !Dungeon.level.heroFOV[cell]
				|| Dungeon.level.distance(h.pos, cell) > alcance
				|| (projetil && new Ballistica(h.pos, cell, Ballistica.PROJECTILE).collisionPos != cell)){
			GLog.w(Messages.get(Renegado.class, "sem_alvo"));
			return null;
		}
		return ch;
	}

	private static void instantanea(Hero h, Amostra a){
		concluir(h, a);
		h.spendAndNext(Actor.TICK);
	}

	private static void executarNova(final Hero h, final Amostra a, int cell, final float p){
		switch (a){
			case TEIA: {
				final Char alvo = alvoADistancia(h, cell, 6, true);
				if (alvo == null) return;
				Buff.prolong(alvo, Roots.class, 4f * p);
				GameScene.add(Blob.seed(alvo.pos, 20, Web.class));
				Sample.INSTANCE.play(Assets.Sounds.MISS);
				instantanea(h, a);
				break;
			}
			case OLHAR: {
				if (cell == h.pos){
					GLog.w(Messages.get(Renegado.class, "sem_alvo"));
					return;
				}
				Ballistica raio = new Ballistica(h.pos, cell, Ballistica.STOP_SOLID);
				int fim = Math.min(raio.dist, 8);
				int dano = Math.round(Random.NormalIntRange(3 + h.lvl, 6 + 2 * h.lvl) * p);
				for (int c : raio.subPath(1, fim)){
					Char ch = Actor.findChar(c);
					if (ch != null && ch != h && ch.alignment != Char.Alignment.ALLY) ch.damage(dano, h);
				}
				if (h.sprite != null && h.sprite.parent != null){
					h.sprite.parent.add(new Beam.DeathRay(h.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(raio.path.get(fim))));
				}
				Sample.INSTANCE.play(Assets.Sounds.RAY);
				instantanea(h, a);
				break;
			}
			case ENCANTO: {
				final Char alvo = alvoADistancia(h, cell, 4, false);
				if (alvo == null) return;
				if (Char.hasProp(alvo, Char.Property.BOSS) || Char.hasProp(alvo, Char.Property.MINIBOSS) || alvo.isImmune(Charm.class)){
					GLog.w(Messages.get(Renegado.class, "encanto_imune"));
				} else {
					Charm c = Buff.affect(alvo, Charm.class, 5f * p);
					c.object = h.id();
					if (alvo.sprite != null) alvo.sprite.centerEmitter().start(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.HEART), 0.2f, 5);
				}
				Sample.INSTANCE.play(Assets.Sounds.CHARMS);
				instantanea(h, a);
				break;
			}
			case FOICES: {
				final Char alvo = inimigoAdjacente(h, cell);
				if (alvo == null) return;
				golpe(h, a, alvo, 0.7f * p, 0, new Callback() {
					@Override
					public void call() {
						if (alvo.isAlive()) h.attack(alvo, 0.7f * p, 0, Char.INFINITE_ACCURACY);
					}
				});
				break;
			}
			case FERRAO: case FERRAO_ACIDO: {
				final Char alvo = alvoADistancia(h, cell, 6, true);
				if (alvo == null) return;
				golpe(h, a, alvo, 1f, 0, new Callback() {
					@Override
					public void call() {
						if (!alvo.isAlive()) return;
						if (a == Amostra.FERRAO) Buff.prolong(alvo, Cripple.class, 3f * p);
						else Buff.affect(alvo, Ooze.class).set(Ooze.DURATION * p);
					}
				});
				break;
			}
			case SEGUNDA_CHANCE:
				Buff.affect(h, SegundaChance.class, 50f).potencia = p;
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				instantanea(h, a);
				break;
			case CARAPACA:
				Buff.affect(h, Carapaca.class, 10f).potencia = p;
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				instantanea(h, a);
				break;
			case COMBUSTAO: {
				int dano = Math.round(Random.NormalIntRange(2 + h.lvl / 2, 5 + h.lvl) * p);
				for (int n : PathFinder.NEIGHBOURS8){
					Char ch = Actor.findChar(h.pos + n);
					if (ch == null || ch.alignment == Char.Alignment.ALLY) continue;
					ch.damage(dano, h);
					if (ch.isAlive()) Buff.affect(ch, Burning.class).reignite(ch, 4f * p);
				}
				if (Dungeon.level.heroFOV[h.pos]) CellEmitter.center(h.pos).burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle.FACTORY, 12);
				Sample.INSTANCE.play(Assets.Sounds.BURNING);
				instantanea(h, a);
				break;
			}
			case SALTO_ESTRIPADOR: {
				Char alvo = alvoADistancia(h, cell, 6, false);
				if (alvo == null) return;
				int destino = celulaAoLado(h, cell);
				if (destino == -1){
					GLog.w(Messages.get(Renegado.class, "sem_espaco"));
					return;
				}
				if (destino != h.pos) ScrollOfTeleportation.appear(h, destino);
				Dungeon.level.occupyCell(h);
				golpe(h, a, alvo, 1.5f * p, 0, null);
				break;
			}
			case FOCO:
				Buff.affect(h, Foco.class, 30f * p);
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				instantanea(h, a);
				break;
			case RASTRO_GELIDO:
				Buff.affect(h, RastroGelido.class).deixar(10f * p);
				Sample.INSTANCE.play(Assets.Sounds.SHATTER);
				instantanea(h, a);
				break;
			default:
				break;
		}
	}

	private static int celulaAoLado(Hero h, int alvo){
		if (Dungeon.level.adjacent(h.pos, alvo)) return h.pos;
		int melhor = -1;
		for (int n : PathFinder.NEIGHBOURS8){
			int c = alvo + n;
			if (!Dungeon.level.passable[c] || Actor.findChar(c) != null) continue;
			if (melhor == -1 || Dungeon.level.trueDistance(h.pos, c) < Dungeon.level.trueDistance(h.pos, melhor)) melhor = c;
		}
		return melhor;
	}

	//---------------------------------------------------------------- medidor

	public static class Medidor extends Buff implements ActionIndicator.Action {
		{
			type = buffType.NEUTRAL;
			revivePersists = true;
		}

		public float valor = 0;
		public int[] lugares = {VAZIO, VAZIO, VAZIO, VAZIO};
		public boolean[] mutado = new boolean[4];
		//quantas vezes chegou a 100 desde o último chefe
		public int contagem = 0;
		//para o continue desfazer a última mutação
		public int ultimoLugar = -1;
		public int ultimaAmostra = VAZIO;
		public boolean assimilado = false;
		public int setorCalma = -1;
		public int calmasUsadas = 0;
		private float inicioSono = -1;

		public int amostra(Lugar l){
			return lugares[l.ordinal()];
		}

		public boolean mutado(Lugar l){
			return mutado[l.ordinal()] || lugares[l.ordinal()] == TECIDO;
		}

		public int travados(){
			int n = 0;
			for (Lugar l : Lugar.values()) if (mutado(l)) n++;
			return n;
		}

		public int piso(){
			int n = 0;
			for (int a : lugares) if (a != VAZIO) n++;
			return 5 * n;
		}

		public void corrigirPiso(){
			valor = Math.max(valor, piso());
		}

		public boolean instalar(Amostra a){
			int i = a.lugar.ordinal();
			if (mutado(a.lugar)) return false;
			lugares[i] = a.ordinal();
			corrigirPiso();
			ActionIndicator.setAction(this);
			return true;
		}

		public Amostra ejetar(Lugar l){
			int i = l.ordinal();
			if (mutado(l) || lugares[i] < 0) return null;
			Amostra a = Amostra.values()[lugares[i]];
			lugares[i] = VAZIO;
			valor = Math.max(piso(), valor - 15);
			return a;
		}

		public void baixar(float v){
			valor = Math.max(piso(), valor - v);
		}

		public void subir(float v){
			if (v <= 0) return;
			Hero h = (Hero) target;
			int lim = limite(h);
			valor = Math.min(lim, valor + v);
			if (valor >= lim) chegou100();
		}

		private void chegou100(){
			Hero h = (Hero) target;
			contagem++;
			if (contagem == 1){
				Buff.affect(h, Surto.class, 10f + 3f * h.pointsInTalent(Talent.SURTO_PROLONGADO));
				valor = piso();
				aviso("cem_surto");
				return;
			}
			if (travados() >= 4){
				assimilado = true;
				valor = piso();
				h.HP = 0;
				h.die(new Assimilacao());
				return;
			}
			escolherMutacao(h);
		}

		private void aviso(final String chave){
			GLog.n(Messages.get(Renegado.class, chave + "_log"));
			if (ShatteredPixelDungeon.scene() instanceof GameScene){
				com.watabou.noosa.Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndTitledMessage(new ItemSprite(ItemSpriteSheet.AMOSTRA),
								Messages.get(Renegado.class, "cem_titulo"), Messages.get(Renegado.class, chave)));
					}
				});
			}
		}

		private void escolherMutacao(final Hero h){
			valor = piso();
			final ArrayList<Lugar> op = new ArrayList<>();
			ArrayList<String> nomes = new ArrayList<>();
			for (Lugar l : Lugar.values()){
				if (mutado(l)) continue;
				op.add(l);
				int a = amostra(l);
				nomes.add(Messages.get(Renegado.class, "lugar_" + l.name().toLowerCase()) + ": "
						+ (a >= 0 ? Amostra.values()[a].nome() : Messages.get(Renegado.class, "vira_tecido")));
			}
			if (op.size() == 1 || !(ShatteredPixelDungeon.scene() instanceof GameScene)){
				mutar(h, op.get(0));
				return;
			}
			final String[] opcoes = nomes.toArray(new String[0]);
			com.watabou.noosa.Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
			GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.AMOSTRA),
					Messages.get(Renegado.class, "mutacao_titulo"), Messages.get(Renegado.class, "mutacao_escolha"),
					opcoes){
				@Override
				protected void onSelect(int index) {
					mutar(h, op.get(index));
				}
				@Override
				public void onBackPressed() {
					//a escolha é obrigatória
				}
			});
				}
			});
		}

		public void mutar(Hero h, Lugar l){
			int i = l.ordinal();
			ultimoLugar = i;
			ultimaAmostra = lugares[i];
			if (lugares[i] == VAZIO) lugares[i] = TECIDO;
			else mutado[i] = true;
			valor = piso();
			//0.9.1: o aviso do fim vem quando as quatro partes já mutaram, porque só a vez seguinte assimila
			if (travados() >= 4) aviso("cem_quarta");
			else aviso("cem_mutacao");
			ActionIndicator.refresh();
		}

		//continue pago: desfaz a última mutação
		public void desfazerUltima(){
			if (ultimoLugar >= 0){
				mutado[ultimoLugar] = false;
				lugares[ultimoLugar] = ultimaAmostra;
				ultimoLugar = -1;
			}
			assimilado = false;
			valor = piso();
		}

		@Override
		public boolean act() {
			Hero h = (Hero) target;
			corrigirPiso();
			if (ActionIndicator.action == null) ActionIndicator.setAction(this);

			//dormir baixa o medidor até a metade, ou menos com os talentos
			if (h.resting){
				if (inicioSono < 0) inicioSono = valor;
				float fracao = 0.5f - 0.1f * h.pointsInTalent(Talent.METABOLISMO_ESTAVEL);
				int vg = h.pointsInTalent(Talent.VIGILIA);
				if (vg >= 3) fracao = 0;
				else if (vg > 0) fracao = Math.min(fracao, 0.5f - 0.15f * vg);
				float alvo = Math.max(piso(), inicioSono * fracao);
				if (valor > alvo) valor = Math.max(alvo, valor - 0.5f);
			} else {
				inicioSono = -1;
			}

			//tecido regenerativo: regeneração extra abaixo de 50
			int tr = h.pointsInTalent(Talent.TECIDO_REGENERATIVO);
			if (tr > 0 && valor < 50 && h.HP < h.HT && Random.Int(10) < tr){
				h.HP++;
			}

			if (Dungeon.level != null){
				int se = h.pointsInTalent(Talent.SENTIDO_ENXAME);
				for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
					//os assimilados reconhecem o desertor na faixa alta
					if (valor >= inicioFaixaAlta(h) && assimilado(m) && m.alignment == Char.Alignment.ENEMY && m.state != m.HUNTING){
						m.beckon(h.pos);
					}
					//sentido do enxame
					if (se > 0 && enxame(m) && Dungeon.level.distance(h.pos, m.pos) <= 4 * se){
						Buff.append(h, TalismanOfForesight.CharAwareness.class, 2f).charID = m.id();
					}
				}
			}
			spend(TICK);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.CORRUPT;
		}

		@Override
		public void tintIcon(Image icon) {
			if (travados() >= 4) icon.hardlight(1f, 0.1f, 0.1f);
			else if (valor > 100) icon.hardlight(1f, 0.25f, 0.1f);
			else if (valor >= 75) icon.hardlight(1f, 0.5f, 0.1f);
			else if (valor >= 50) icon.hardlight(1f, 0.9f, 0.3f);
			else icon.hardlight(0.5f, 1f, 0.4f);
		}

		@Override
		public float iconFadePercent() {
			return 1f - Math.min(100, valor) / 100f;
		}

		@Override
		public String iconTextDisplay() {
			//acima de 100, mostra quanto da tolerância já foi usado
			if (valor > 100) return "100+" + ((int) valor - 100);
			return Integer.toString((int) valor);
		}

		@Override
		public String desc() {
			Hero h = (Hero) target;
			StringBuilder sb = new StringBuilder(Messages.get(this, "desc", (int) valor, piso(), contagem, limite(h)));
			for (Lugar l : Lugar.values()){
				int a = amostra(l);
				String nome = a >= 0 ? Amostra.values()[a].nome() : (a == TECIDO ? Messages.get(Renegado.class, "tecido") : Messages.get(Renegado.class, "vazio"));
				if (mutado[l.ordinal()]) nome += " " + Messages.get(Renegado.class, "mutada");
				sb.append("\n").append(Messages.get(Renegado.class, "lugar_" + l.name().toLowerCase())).append(": ").append(nome);
			}
			if (travados() >= 4) sb.append("\n\n").append(Messages.get(this, "desc_perigo"));
			return sb.toString();
		}

		//botão de ação: abre a lista das amostras instaladas
		@Override
		public String actionName() {
			return Messages.get(Renegado.class, "acao");
		}

		@Override
		public int actionIcon() {
			return HeroIcon.AMOSTRAS;
		}

		@Override
		public int indicatorColor() {
			return travados() >= 4 ? 0x880000 : 0x335522;
		}

		@Override
		public void doAction() {
			final Hero h = (Hero) target;
			final ArrayList<Amostra> op = new ArrayList<>();
			ArrayList<String> nomes = new ArrayList<>();
			for (Lugar l : Lugar.values()){
				int a = amostra(l);
				if (a < 0) continue;
				Amostra am = Amostra.values()[a];
				op.add(am);
				nomes.add(am.habilidade() + " (+" + custo(h, am) + ")");
			}
			nomes.add(Messages.get(Renegado.class, "ejetar"));
			GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.AMOSTRA),
					Messages.get(Renegado.class, "titulo"), Messages.get(Renegado.class, "lista", (int) valor),
					nomes.toArray(new String[0])){
				@Override
				protected void onSelect(int index) {
					if (index < op.size()) usar(h, op.get(index));
					else escolherEjecao(h);
				}
			});
		}

		private void escolherEjecao(final Hero h){
			final ArrayList<Lugar> op = new ArrayList<>();
			ArrayList<String> nomes = new ArrayList<>();
			for (Lugar l : Lugar.values()){
				if (mutado(l) || amostra(l) < 0) continue;
				op.add(l);
				nomes.add(Amostra.values()[amostra(l)].nome());
			}
			if (op.isEmpty()){
				GLog.w(Messages.get(Renegado.class, "nada_ejetar"));
				return;
			}
			GameScene.show(new WndOptions(Messages.get(Renegado.class, "ejetar"), Messages.get(Renegado.class, "ejetar_desc"),
					nomes.toArray(new String[0])){
				@Override
				protected void onSelect(int index) {
					Amostra a = ejetar(op.get(index));
					if (a == null) return;
					int ej = h.pointsInTalent(Talent.EJECAO_LIMPA);
					if (ej >= 2 || (ej == 1 && Random.Int(2) == 0)){
						AmostraItem it = new AmostraItem().tipo(a);
						if (!it.collect()) Dungeon.level.drop(it, h.pos);
					}
					GLog.i(Messages.get(Renegado.class, "ejetada", a.nome()));
					h.spendAndNext(Actor.TICK);
				}
			});
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("valor", valor);
			bundle.put("lugares", lugares);
			bundle.put("mutado", mutado);
			bundle.put("contagem", contagem);
			bundle.put("ultimoLugar", ultimoLugar);
			bundle.put("ultimaAmostra", ultimaAmostra);
			bundle.put("assimilado", assimilado);
			bundle.put("setorCalma", setorCalma);
			bundle.put("calmasUsadas", calmasUsadas);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			valor = bundle.getFloat("valor");
			lugares = bundle.getIntArray("lugares");
			mutado = bundle.getBooleanArray("mutado");
			contagem = bundle.getInt("contagem");
			ultimoLugar = bundle.getInt("ultimoLugar");
			ultimaAmostra = bundle.getInt("ultimaAmostra");
			assimilado = bundle.getBoolean("assimilado");
			setorCalma = bundle.getInt("setorCalma");
			calmasUsadas = bundle.getInt("calmasUsadas");
			ActionIndicator.setAction(this);
		}
	}

	//causa da morte por assimilação
	public static class Assimilacao {}

	public static void aoContinuar(Hero h){
		Medidor m = h.buff(Medidor.class);
		if (m != null && m.assimilado) m.desfazerUltima();
	}

	//---------------------------------------------------------------- buffs

	public static class Surto extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = true;
		}

		@Override
		public boolean act() {
			Hero h = (Hero) target;
			//ataca o que estiver mais perto; o Híbrido escolhe os alvos
			if (h.subClass != HeroSubClass.CHAMPION && Dungeon.level != null){
				Char perto = null;
				for (int n : PathFinder.NEIGHBOURS8){
					Char ch = Actor.findChar(h.pos + n);
					if (ch != null && ch != h) { perto = ch; break; }
				}
				if (perto != null) h.curAction = new HeroAction.Attack(perto);
			}
			return super.act();
		}

		@Override
		public int icon() {
			return BuffIndicator.BERSERK;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / 10f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}
	}

	public static class Gelatinoso extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.4f, 1f, 0.4f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / 10f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}
	}

	//forma do enxame: habilidades sem subir o medidor, e um salto no fim
	public static class FormaEnxame extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public boolean act() {
			Hero h = (Hero) target;
			int r = h.pointsInTalent(Talent.REGENERACAO_ENXAME);
			if (r > 0 && h.HP < h.HT) h.HP = Math.min(h.HT, h.HP + Math.max(1, Math.round(h.HT * 0.01f * r)));
			return super.act();
		}

		@Override
		public void detach() {
			super.detach();
			if (target instanceof Hero && target.isAlive()){
				Hero h = (Hero) target;
				Medidor m = medidor(h);
				if (m != null) m.subir(30 - 5 * h.pointsInTalent(Talent.SALTO_CONTIDO));
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.HASTE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.5f, 1f, 0.3f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}
	}

	//amostras do tronco e das pernas que duram alguns turnos
	public static class SegundaChance extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		public float potencia = 1f;

		@Override
		public int icon() {
			return BuffIndicator.ANKH;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.5f, 1f, 0.4f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / 50f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(20 * potencia), dispTurns());
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("potencia", potencia);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			potencia = bundle.getFloat("potencia");
		}
	}

	public static class Carapaca extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		public float potencia = 1f;

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.8f, 0.6f, 0.3f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / 10f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("potencia", potencia);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			potencia = bundle.getFloat("potencia");
		}
	}

	public static class Foco extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.DUEL_EVASIVE;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / 30f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}
	}

	//rastro gélido: o chão que ele deixa para trás congela por dois turnos
	public static class RastroGelido extends Buff {
		{
			type = buffType.POSITIVE;
		}

		private float restante = 0;
		private int ultimo = -1;

		public void deixar(float turnos){
			restante = Math.max(restante, turnos);
			ultimo = target.pos;
		}

		@Override
		public boolean act() {
			if (ultimo != -1 && ultimo != target.pos && Actor.findChar(ultimo) == null && !Dungeon.level.solid[ultimo]){
				GameScene.add(Blob.seed(ultimo, 2, com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing.class));
			}
			ultimo = target.pos;
			restante -= TICK;
			if (restante <= 0) detach();
			spend(TICK);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.FROST;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - restante / 10f);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int) restante);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int) restante);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("restante", restante);
			bundle.put("ultimo", ultimo);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			restante = bundle.getFloat("restante");
			ultimo = bundle.getInt("ultimo");
		}
	}

	//golpe da assimilação forçada: a amostra cai com certeza se o golpe matar
	public static class AssimilacaoGarantida extends FlavourBuff {
	}

	//feromônio de comando: lentidão no fim, com exaustão
	public static class Exaustao extends FlavourBuff {
		public int pontos = 0;

		@Override
		public void detach() {
			super.detach();
			if (target.isAlive() && pontos > 0) Buff.affect(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow.class, 2f * pontos);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("pontos", pontos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pontos = bundle.getInt("pontos");
		}
	}
}
