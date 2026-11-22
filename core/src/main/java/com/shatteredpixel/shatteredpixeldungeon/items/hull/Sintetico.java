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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.AbrirFechadura;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.Autorreparo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.BarreiraTemporaria;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.ChamarDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.DescargaEletrica;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.DesligarArmadilha;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.InvadirMaquina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.ReligarTerminal;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.RevelarArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico.TrancarPorta;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashSet;

//Shattered Hull: o Sintético. Reserva de energia no lugar da fome, habilidades pagas com energia,
//imunidades e a fraqueza elétrica.
public class Sintetico {

	public static final int AVISO_BAIXA = 150;
	public static final int ABATE = 25;
	public static final int ABATE_CAMPEAO = 75;
	//ajustados por Pablo em 21/11/2026, depois da simulação de energia
	public static final int POR_NIVEL = 5;
	public static final int POR_PONTO_RESERVA = 40;

	public static boolean e(Char c){
		return c instanceof Hero && ((Hero) c).heroClass == HeroClass.CLERIC;
	}

	public static Reserva reserva(Hero h){
		if (!e(h)) return null;
		Reserva r = h.buff(Reserva.class);
		if (r == null) r = Buff.affect(h, Reserva.class);
		return r;
	}

	public static float energia(Hero h){
		Reserva r = reserva(h);
		return r == null ? 0 : r.energia();
	}

	public static boolean economia(Char c){
		if (!e(c)) return false;
		Reserva r = c.buff(Reserva.class);
		return r != null && r.energia() <= 0;
	}

	//---------------------------------------------------------------- habilidades do núcleo

	public static ArrayList<ClericSpell> habilidades(Hero h, int nivel){
		ArrayList<ClericSpell> lista = new ArrayList<>();
		if (nivel == 1){
			lista.add(DescargaEletrica.INSTANCE);
			lista.add(DesligarArmadilha.INSTANCE);
			lista.add(AbrirFechadura.INSTANCE);
			lista.add(Autorreparo.INSTANCE);
		} else if (nivel == 2 && h.lvl >= 6){
			lista.add(RevelarArea.INSTANCE);
			lista.add(TrancarPorta.INSTANCE);
			lista.add(BarreiraTemporaria.INSTANCE);
			lista.add(ReligarTerminal.INSTANCE);
			lista.add(ChamarDrone.INSTANCE);
		} else if (nivel == 3 && h.lvl >= 12){
			lista.add(InvadirMaquina.INSTANCE);
		}
		return lista;
	}

	//custo final de uma habilidade: a sobrecarga do núcleo corta pela metade, e o primeiro disparo
	//deixa de graça o primeiro uso de cada habilidade durante a sobrecarga
	public static int custo(Hero h, int base, ClericSpell s){
		Sobrecarga sc = h.buff(Sobrecarga.class);
		if (sc != null){
			if (sc.gratis(h, s)) return 0;
			return (int)Math.ceil(base / 2f);
		}
		return base;
	}

	public static void registrarUso(Hero h, ClericSpell s){
		Sobrecarga sc = h.buff(Sobrecarga.class);
		if (sc != null) sc.usadas.add(s.getClass().getSimpleName());
	}

	public static void gastar(Hero h, float quanto){
		Reserva r = reserva(h);
		if (r == null || quanto <= 0) return;
		r.gastar(quanto);
		//couraçado: 30% da energia gasta vira escudo
		if (h.subClass == HeroSubClass.PALADIN){
			int escudo = Math.round(quanto * 0.3f);
			if (escudo > 0) Buff.affect(h, Barrier.class).incShield(escudo);
		}
	}

	public static void ganhar(Hero h, float quanto){
		Reserva r = reserva(h);
		if (r != null && quanto > 0) r.ganhar(quanto);
	}

	//---------------------------------------------------------------- recarga por abate

	public static boolean maquina(Char ch){
		return ch != null && Char.hasProp(ch, Char.Property.INORGANIC);
	}

	public static void abate(Mob m, Object causa){
		Hero h = Dungeon.hero;
		if (!e(h) || m.alignment != Char.Alignment.ENEMY) return;
		Reserva r = reserva(h);
		//um chefe devolve metade da reserva máxima
		if (Char.hasProp(m, Char.Property.BOSS)){
			r.ganhar(r.max() / 2f);
			return;
		}
		float v = m.buff(ChampionEnemy.class) != null ? ABATE_CAMPEAO : ABATE;
		v *= 1f + 0.2f * h.pointsInTalent(Talent.EFICIENCIA_ENERGETICA);
		boolean aliado = causa instanceof Char && causa != h && ((Char) causa).alignment == Char.Alignment.ALLY;
		if (aliado){
			switch (h.pointsInTalent(Talent.RETORNO_SINAL)){
				case 0: default: v = (int)(v / 2f); break;
				case 1: v *= 16f / 25f; break;
				case 2: v *= 20f / 25f; break;
				case 3: break;
			}
		}
		r.ganhar(Math.round(v));
	}

	//---------------------------------------------------------------- corpo de máquina

	private static final HashSet<Class> IMUNIDADES = new HashSet<>();
	static {
		IMUNIDADES.add(Poison.class);
		IMUNIDADES.add(Bleeding.class);
		IMUNIDADES.add(Charm.class);
		IMUNIDADES.add(Shaman.EarthenBolt.class);
	}

	public static boolean imune(Char c, Class efeito){
		if (!e(c)) return false;
		for (Class i : IMUNIDADES) if (i.isAssignableFrom(efeito)) return true;
		return false;
	}

	public static boolean eletrico(Object src){
		if (src == null) return false;
		Class c = src instanceof Class ? (Class) src : src.getClass();
		for (Class e : Char.Property.ELECTRIC.resistances()) if (e.isAssignableFrom(c)) return true;
		return src instanceof MunicaoEspecial.Choque;
	}

	public static float fraquezaEletrica(Hero h){
		float f = h.subClass == HeroSubClass.PALADIN ? 0.25f : 0.5f;
		f -= 0.15f * h.pointsInTalent(Talent.ISOLAMENTO);
		return Math.max(0, f);
	}

	//dano recebido pelo Sintético: fraqueza elétrica e para-raios
	public static int danoRecebido(Hero h, int dmg, Object src){
		if (!e(h) || dmg <= 0 || !eletrico(src)) return dmg;
		dmg = Math.round(dmg * (1f + fraquezaEletrica(h)));
		int pr = h.pointsInTalent(Talent.PARA_RAIOS);
		if (pr > 0) ganhar(h, dmg * 0.2f * pr);
		return dmg;
	}

	//reciclagem: comida e injetores valem mais na venda
	public static int valorVenda(com.shatteredpixel.shatteredpixeldungeon.items.Item item){
		int v = item.value();
		Hero h = Dungeon.hero;
		if (h != null && e(h) && (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food
				|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing)){
			v = Math.round(v * (1f + 0.5f * h.pointsInTalent(Talent.RECICLAGEM)));
		}
		return v;
	}

	//reparo de campo: cada unidade de biomassa repara mais
	public static float fatorKit(Hero h){
		if (!e(h)) return 1f;
		return 1f + 0.25f * h.pointsInTalent(Talent.REPARO_CAMPO);
	}

	public static final int CUSTO_GOLPE = 10;

	//golpe energizado: cada golpe com a chave de torque gasta 10 de energia por mais dano,
	//só com a reserva acima do aviso de reserva baixa
	public static int golpeEnergizado(Hero h, Char inimigo, int dano){
		int p = h.pointsInTalent(Talent.GOLPE_ENERGIZADO);
		if (p == 0 || !e(h) || inimigo == null) return dano;
		if (!(h.belongings.attackingWeapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel)) return dano;
		if (energia(h) < AVISO_BAIXA + CUSTO_GOLPE) return dano;
		gastar(h, CUSTO_GOLPE);
		if (inimigo.sprite != null) inimigo.sprite.centerEmitter().burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle.FACTORY, 3);
		return dano + 1 + p;
	}

	public static final int CUSTO_INVESTIDA = 20;

	//investida hidráulica: o golpe com a chave de torque empurra e atordoa, por 20 de energia
	public static void investida(Hero h, Char inimigo){
		int p = h.pointsInTalent(Talent.INVESTIDA_HIDRAULICA);
		if (p == 0 || !e(h) || inimigo == null || !inimigo.isAlive()) return;
		if (!(h.belongings.attackingWeapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel)) return;
		if (energia(h) < CUSTO_INVESTIDA || economia(h)) return;
		gastar(h, CUSTO_INVESTIDA);
		Buff.prolong(inimigo, Paralysis.class, p);
		if (!Char.hasProp(inimigo, Char.Property.IMMOVABLE)){
			int dir = inimigo.pos - h.pos;
			int destino = inimigo.pos + dir;
			com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica traj =
					new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(inimigo.pos, destino + dir * (p - 1), com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT);
			com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave.throwChar(inimigo, traj, p, false, true, h);
		}
	}

	//---------------------------------------------------------------- buffs

	public static class Reserva extends Buff {
		{
			type = buffType.NEUTRAL;
			revivePersists = true;
		}

		private float energia = -1;
		private boolean apresentado = false;
		private boolean avisoBaixa = false;
		private boolean avisoBaixaJanela = false;
		private boolean avisoEconomia = false;

		public int max(){
			Hero h = (Hero) target;
			if (h == null) return 600;
			return 600 + POR_NIVEL * h.lvl + POR_PONTO_RESERVA * h.pointsInTalent(Talent.RESERVA_EXPANDIDA);
		}

		public float energia(){
			if (energia < 0) energia = max();
			return energia;
		}

		public void encher(){
			energia = max();
		}

		public void ganhar(float v){
			boolean vazia = energia() <= 0;
			energia = Math.min(max(), energia() + v);
			if (target != null && target.sprite != null && v >= 1){
				target.sprite.showStatus(0x44CCFF, "+%d", Math.round(v));
			}
			if (vazia && energia > 0) GLog.p(Messages.get(Sintetico.class, "economia_fim"));
		}

		public void gastar(float v){
			energia = Math.max(0, energia() - v);
		}

		@Override
		public boolean act() {
			if (!apresentado){
				apresentado = true;
				if (ShatteredPixelDungeon.scene() instanceof GameScene){
					com.watabou.noosa.Game.runOnRenderThread(new com.watabou.utils.Callback() { @Override public void call() { GameScene.show(new WndTitledMessage(new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME),
							Messages.get(Sintetico.class, "intro_titulo"), Messages.get(Sintetico.class, "intro"))); } });
				}
			}

			boolean pausa = Dungeon.level == null || Dungeon.level.locked || SPDSettings.intro()
					|| target.buff(ScrollOfChallenge.ChallengeArena.class) != null;
			if (!pausa && target.isAlive()){
				energia = Math.max(0, energia() - 1);
			}

			if (energia() < AVISO_BAIXA && energia > 0){
				if (!avisoBaixa){
					avisoBaixa = true;
					GLog.w(Messages.get(Sintetico.class, "baixa"));
					if (!avisoBaixaJanela && ShatteredPixelDungeon.scene() instanceof GameScene){
						avisoBaixaJanela = true;
						com.watabou.noosa.Game.runOnRenderThread(new com.watabou.utils.Callback() { @Override public void call() { GameScene.show(new WndTitledMessage(new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME),
								Messages.get(Sintetico.class, "baixa_titulo"), Messages.get(Sintetico.class, "baixa_janela"))); } });
					}
				}
			} else if (energia >= AVISO_BAIXA){
				avisoBaixa = false;
			}

			if (energia <= 0){
				if (!avisoEconomia){
					avisoEconomia = true;
					GLog.n(Messages.get(Sintetico.class, "economia"));
				}
			} else {
				avisoEconomia = false;
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
			if (energia() <= 0) icon.hardlight(1f, 0.2f, 0.2f);
			else if (energia < AVISO_BAIXA) icon.hardlight(1f, 0.8f, 0.2f);
			else icon.hardlight(0.3f, 0.8f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - energia() / max());
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int) energia());
		}

		@Override
		public String name() {
			return Messages.get(this, energia() <= 0 ? "name_economia" : "name");
		}

		@Override
		public String desc() {
			String d = Messages.get(this, "desc", (int) energia(), max());
			if (energia() <= 0) d += "\n\n" + Messages.get(this, "desc_economia");
			return d;
		}

		private static final String ENERGIA = "energia", APRESENTADO = "apresentado", BAIXA = "baixa", JANELA = "janela", ECONOMIA = "economia";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ENERGIA, energia);
			bundle.put(APRESENTADO, apresentado);
			bundle.put(BAIXA, avisoBaixa);
			bundle.put(JANELA, avisoBaixaJanela);
			bundle.put(ECONOMIA, avisoEconomia);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			energia = bundle.getFloat(ENERGIA);
			apresentado = bundle.getBoolean(APRESENTADO);
			avisoBaixa = bundle.getBoolean(BAIXA);
			avisoBaixaJanela = bundle.getBoolean(JANELA);
			avisoEconomia = bundle.getBoolean(ECONOMIA);
		}
	}

	//autorreparo: 30% da vida máxima em 10 turnos
	public static class Reparo extends Buff {
		{
			type = buffType.POSITIVE;
		}

		public int turnos = 10;
		private float parcial = 0;

		@Override
		public boolean act() {
			Hero h = (Hero) target;
			if (economia(h)){
				detach();
				return true;
			}
			parcial += h.HT * 0.03f;
			int cura = (int) parcial;
			parcial -= cura;
			if (cura > 0 && h.HP < h.HT){
				cura = Math.min(cura, h.HT - h.HP);
				h.HP += cura;
				if (h.sprite != null) h.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(cura), com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText.HEALING);
			}
			turnos--;
			if (turnos <= 0){
				int pp = h.pointsInTalent(Talent.PLACAS_REPARO);
				if (pp > 0) Buff.affect(h, Barrier.class).setShield(Math.round(h.HT * 0.1f * pp));
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public float iconFadePercent() {
			return 1f - turnos / 10f;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(turnos);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", turnos);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("turnos", turnos);
			bundle.put("parcial", parcial);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			turnos = bundle.getInt("turnos");
			parcial = bundle.getFloat("parcial");
		}
	}

	//máquina invadida: luta do lado do Sintético até o tempo acabar
	public static class Invadido extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = true;
		}

		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)){
				target.alignment = Char.Alignment.ALLY;
				if (target instanceof Mob){
					Mob m = (Mob) target;
					m.state = m.HUNTING;
				}
				return true;
			}
			return false;
		}

		@Override
		public void detach() {
			super.detach();
			if (target.isAlive()){
				target.alignment = Char.Alignment.ENEMY;
				if (target instanceof Mob){
					((Mob) target).state = ((Mob) target).HUNTING;
				}
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.CORRUPT;
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

	public static float duracaoInvasao(Hero h){
		float d = 15f + 5f * h.pointsInTalent(Talent.INVASAO_PROLONGADA);
		if (h.subClass == HeroSubClass.PRIEST) d *= 1.5f;
		return d;
	}

	//sobrecarga do núcleo
	public static class Sobrecarga extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		public static final float DURACAO = 15f;
		public static final int QUEDA = 150;

		public HashSet<String> usadas = new HashSet<>();

		public boolean gratis(Hero h, ClericSpell s){
			int p = h.pointsInTalent(Talent.PRIMEIRO_DISPARO);
			if (p == 0 || usadas.contains(s.getClass().getSimpleName())) return false;
			return p >= 4 || usadas.size() < p;
		}

		public float velocidade(Hero h){
			return 1.25f + 0.1f * h.pointsInTalent(Talent.VELOCIDADE_EXTREMA);
		}

		@Override
		public void detach() {
			super.detach();
			if (target instanceof Hero && target.isAlive()){
				Hero h = (Hero) target;
				float q = QUEDA * (1f - Math.min(0.75f, 0.25f * h.pointsInTalent(Talent.DISSIPACAO_CONTROLADA)));
				Reserva r = reserva(h);
				if (r != null) r.gastar(q);
				GLog.w(Messages.get(this, "fim", Math.round(q)));
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.HASTE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.3f, 0.8f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, 1f - visualcooldown() / DURACAO);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns());
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("usadas", usadas.toArray(new String[0]));
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			usadas.clear();
			if (bundle.contains("usadas")) for (String u : bundle.getStringArray("usadas")) usadas.add(u);
		}
	}

	//campo residual do pulso eletromagnético
	public static class CampoResidual extends Buff {
		public int[] celulas = new int[0];
		public int turnos;

		@Override
		public boolean act() {
			for (int c : celulas){
				Char ch = Actor.findChar(c);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) Buff.prolong(ch, Paralysis.class, 1f);
			}
			turnos--;
			if (turnos <= 0) detach();
			else spend(TICK);
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("celulas", celulas);
			bundle.put("turnos", turnos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			celulas = bundle.getIntArray("celulas");
			turnos = bundle.getInt("turnos");
		}
	}
}
