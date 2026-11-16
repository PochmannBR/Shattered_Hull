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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

//Shattered Hull: dados de reconhecimento das Forças especiais e o ruído dos tiros.
//Cada abate furtivo revela o mapa em volta do inimigo; no terceiro abate furtivo
//do mesmo setor, o setor inteiro aparece no mapa.
public class ReconData extends Buff {

	public static final int ABATES_PARA_O_SETOR = 3;
	public static final int RAIO_BASE = 5;
	public static final int RAIO_RUIDO_FUZIL = 6;

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public int depth = -1;
	public int abates = 0;

	@Override
	public int icon() {
		return BuffIndicator.MIND_VISION;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.min(abates, ABATES_PARA_O_SETOR));
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (ABATES_PARA_O_SETOR - abates) / (float) ABATES_PARA_O_SETOR);
	}

	@Override
	public boolean act() {
		//os dados valem só para o setor onde foram coletados
		if (depth != Dungeon.depth){
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}

	@Override
	public String desc() {
		if (abates >= ABATES_PARA_O_SETOR){
			return Messages.get(this, "desc_completo");
		}
		return Messages.get(this, "desc", abates, ABATES_PARA_O_SETOR);
	}

	public static int raio( Hero hero ){
		return RAIO_BASE + 2 * hero.pointsInTalent(Talent.DEEP_RECON);
	}

	//chamado pelo Char.attack quando o herói mata um inimigo que não o tinha visto
	public static void abateFurtivo( Hero hero, Char enemy ){
		if (hero.heroClass != HeroClass.ROGUE || !(enemy instanceof Mob)) return;
		if (enemy.alignment != Char.Alignment.ENEMY) return;

		ReconData dados = hero.buff(ReconData.class);
		if (dados != null && dados.depth != Dungeon.depth){
			dados.detach();
			dados = null;
		}
		if (dados == null){
			dados = Buff.affect(hero, ReconData.class);
			dados.depth = Dungeon.depth;
		}
		dados.abates++;

		if (dados.abates == ABATES_PARA_O_SETOR){
			revelar(-1, 0);
			GLog.p(Messages.get(ReconData.class, "setor_completo"));
		} else if (dados.abates < ABATES_PARA_O_SETOR){
			revelar(enemy.pos, raio(hero));
			GLog.i(Messages.get(ReconData.class, "coletados", dados.abates, ABATES_PARA_O_SETOR));
		}
		BuffIndicator.refreshHero();

		//sumir na confusão: alguns turnos de invisibilidade depois do abate
		int pontos = hero.pointsInTalent(Talent.VANISH_IN_CHAOS);
		if (pontos > 0){
			Buff.prolong(hero, Invisibility.class, 1 + pontos);
		}
	}

	//revela o mapa num raio em volta de uma célula, ou o setor inteiro com centro -1
	public static void revelar( int centro, int raio ){
		int length = Dungeon.level.length();
		boolean[] mapped = Dungeon.level.mapped;
		boolean[] discoverable = Dungeon.level.discoverable;
		for (int i = 0; i < length; i++){
			if (!discoverable[i]) continue;
			if (centro >= 0 && Dungeon.level.distance(centro, i) > raio) continue;
			mapped[i] = true;
		}
		GameScene.updateFog();
		Sample.INSTANCE.play(Assets.Sounds.READ, 0.5f, 1.3f);
	}

	//o tiro do fuzil acorda e atrai os inimigos próximos até o lugar do disparo
	public static void ruido( int pos, int raio ){
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
			if (mob.alignment != Char.Alignment.ENEMY) continue;
			if (mob.state == mob.HUNTING || mob.state == mob.FLEEING) continue;
			if (Dungeon.level.distance(pos, mob.pos) > raio) continue;
			if (mob.paralysed > 0) continue;
			mob.ouvirRuido(pos);
		}
	}

	private static final String DEPTH  = "depth";
	private static final String ABATES = "abates";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(DEPTH, depth);
		bundle.put(ABATES, abates);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		depth = bundle.getInt(DEPTH);
		abates = bundle.getInt(ABATES);
	}
}
