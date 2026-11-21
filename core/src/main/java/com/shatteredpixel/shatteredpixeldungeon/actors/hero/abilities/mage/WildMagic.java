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
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.MunicaoEspecial;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.PedidoSuprimento;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WildMagic extends ArmorAbility {

	{
		baseChargeUse = 25f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		if (target == hero.pos){
			GLog.w(Messages.get(this, "self_target"));
			return;
		}

		//Shattered Hull: rajada do transmissor. Dispara a arma especial acoplada ao transmissor
		//orbital várias vezes seguidas, sem gastar carga
		ArrayList<Wand> wands = new ArrayList<>();
		MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
		Wand acoplada = staff == null ? null : staff.wandAcoplada();
		if (acoplada != null){
			int disparos = 3 + hero.pointsInTalent(Talent.RAJADA_LONGA);
			for (int i = 0; i < disparos; i++) wands.add(acoplada);
		}

		if (wands.size() == 0){
			GLog.w(Messages.get(this, "no_wands"));
			return;
		}

		hero.busy();

		Buff.affect(hero, WildMagicTracker.class, 0f);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		zapWand(wands, hero, target);

	}

	public static class WildMagicTracker extends FlavourBuff{};

	Actor wildMagicActor = null;

	//alvo do disparo atual, para a retroalimentação saber se ele morreu
	private Char alvoAtual = null;

	private void zapWand( ArrayList<Wand> wands, Hero hero, int cell){
		Wand cur = wands.remove(0);
		alvoAtual = Actor.findChar(cell);

		Ballistica aim = new Ballistica(hero.pos, cell, cur.collisionProperties(cell));

		hero.sprite.zap(cell);

		float startTime = Game.timeTotal;
		if (cur.tryToZap(hero, cell)) {
			if (!cur.cursed) {
				cur.fx(aim, new Callback() {
					@Override
					public void call() {
						cur.onZap(aim);
						boolean alsoCursedZap = Random.Float() < WondrousResin.extraCurseEffectChance();
						if (Game.timeTotal - startTime < 0.33f) {
							hero.sprite.parent.add(new Delayer(0.33f - (Game.timeTotal - startTime)) {
								@Override
								protected void onComplete() {
									if (alsoCursedZap){
										WondrousResin.forcePositive = true;
										CursedWand.cursedZap(cur,
												hero,
												new Ballistica(hero.pos, cell, Ballistica.MAGIC_BOLT),
												new Callback() {
													@Override
													public void call() {
														WondrousResin.forcePositive = false;
														afterZap(cur, wands, hero, cell);
													}
												});
									} else {
										afterZap(cur, wands, hero, cell);
									}
								}
							});
						} else {
							if (alsoCursedZap){
								WondrousResin.forcePositive = true;
								CursedWand.cursedZap(cur,
										hero,
										new Ballistica(hero.pos, cell, Ballistica.MAGIC_BOLT),
										new Callback() {
											@Override
											public void call() {
												WondrousResin.forcePositive = false;
												afterZap(cur, wands, hero, cell);
											}
										});
							} else {
								afterZap(cur, wands, hero, cell);
							}
						}
					}
				});

			} else {
				CursedWand.cursedZap(cur,
						hero,
						new Ballistica(hero.pos, cell, Ballistica.MAGIC_BOLT),
						new Callback() {
							@Override
							public void call() {
								if (Game.timeTotal - startTime < 0.33f) {
									hero.sprite.parent.add(new Delayer(0.33f - (Game.timeTotal - startTime)) {
										@Override
										protected void onComplete() {
											afterZap(cur, wands, hero, cell);
										}
									});
								} else {
									afterZap(cur, wands, hero, cell);
								}
							}
						});
			}
		} else {
			afterZap(cur, wands, hero, cell);
		}
	}

	private void afterZap( Wand cur, ArrayList<Wand> wands, Hero hero, int target){
		//Shattered Hull: a rajada não gasta carga. A munição acoplada aplica a munição especial,
		//e a retroalimentação pode devolver uma carga de pedido a cada inimigo morto
		if (alvoAtual != null){
			MunicaoEspecial.Ativa municao = hero.buff(MunicaoEspecial.Ativa.class);
			if (municao != null && alvoAtual.isAlive() && Random.Int(4) < hero.pointsInTalent(Talent.MUNICAO_ACOPLADA)){
				MunicaoEspecial.aplicar(municao.efeito, alvoAtual);
			}
			if (!alvoAtual.isAlive() && alvoAtual.alignment == Char.Alignment.ENEMY
					&& Random.Int(4) < hero.pointsInTalent(Talent.RETROALIMENTACAO)){
				PedidoSuprimento.Cargas c = PedidoSuprimento.cargas(hero);
				if (c != null) c.ganhar(1);
			}
			alvoAtual = null;
		}
		if (wildMagicActor != null){
			wildMagicActor.next();
			wildMagicActor = null;
		}

		Char ch = Actor.findChar(target);
		if (!wands.isEmpty() && hero.isAlive()) {
			Actor.add(new Actor() {
				{
					actPriority = VFX_PRIO-1;
				}

				@Override
				protected boolean act() {
					wildMagicActor = this;
					zapWand(wands, hero, ch == null ? target : ch.pos);
					Actor.remove(this);
					return false;
				}
			});
			hero.next();
		} else {
			if (hero.buff(WildMagicTracker.class) != null) {
				hero.buff(WildMagicTracker.class).detach();
			}
			Item.updateQuickslot();
			Invisibility.dispel();
			hero.spendAndNext(Actor.TICK);
		}
	}

	@Override
	public int icon() {
		return HeroIcon.WILD_MAGIC;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.RAJADA_LONGA, Talent.MUNICAO_ACOPLADA, Talent.RETROALIMENTACAO, Talent.HEROIC_ENERGY};
	}
}
