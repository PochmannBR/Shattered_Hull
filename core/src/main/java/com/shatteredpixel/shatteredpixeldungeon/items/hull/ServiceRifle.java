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


package com.shatteredpixel.shatteredpixeldungeon.items.hull;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: fuzil de serviço do Fuzileiro.
//Atirar gasta munição do carregador inserido. Atacar corpo a corpo é a coronhada.
public class ServiceRifle extends MeleeWeapon {

	public static final String AC_SHOOT  = "SHOOT";
	public static final String AC_RELOAD = "RELOAD";

	public int rounds = Magazine.CAPACITY;

	{
		image = ItemSpriteSheet.SERVICE_RIFLE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.1f;

		tier = 1;

		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	//coronhada: mais fraca que uma espada curta
	@Override
	public int min(int lvl) {
		return 1 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 5 + lvl;
	}

	public int bulletMin(){
		return 3 + buffedLvl();
	}

	public int bulletMax(){
		return 9 + 2*buffedLvl();
	}

	@Override
	public String defaultAction() {
		return AC_SHOOT;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SHOOT);
		actions.add(AC_RELOAD);
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ) {
		if (action.equals(AC_SHOOT) || action.equals(AC_RELOAD)){
			return Messages.upperCase(Messages.get(this, "ac_" + action.toLowerCase()));
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_SHOOT)) {
			usesTargeting = true;
			if (rounds <= 0){
				if (bestMagazine(hero) != null){
					reload(hero);
				} else {
					GLog.w(Messages.get(this, "empty"));
				}
				return;
			}
			curUser = hero;
			curItem = this;
			GameScene.selectCell( shooter );

		} else if (action.equals(AC_RELOAD)) {
			reload(hero);
		}
	}

	private Magazine bestMagazine( Hero hero ){
		Magazine best = null;
		for (Magazine m : hero.belongings.getAllItems(Magazine.class)){
			if (best == null || m.rounds > best.rounds) best = m;
		}
		return best;
	}

	//recarga antecipada: o carregador parcial volta para a mochila
	public void reload( Hero hero ){
		if (rounds >= Magazine.CAPACITY){
			GLog.i(Messages.get(this, "already_full"));
			return;
		}
		Magazine best = bestMagazine(hero);
		if (best == null){
			GLog.w(Messages.get(this, "no_magazines"));
			return;
		}
		if (best.rounds <= rounds){
			GLog.w(Messages.get(this, "no_better_magazine"));
			return;
		}

		Magazine inserted = (Magazine) best.detach(hero.belongings.backpack);
		int old = rounds;
		rounds = inserted.rounds;

		if (old > 0){
			Magazine partial = new Magazine(old);
			if (!partial.collect(hero.belongings.backpack)){
				Dungeon.level.drop(partial, hero.pos).sprite.drop();
			}
			GLog.i(Messages.get(this, "reload_partial", old));
		} else {
			GLog.i(Messages.get(this, "reload"));
		}

		Sample.INSTANCE.play(Assets.Sounds.UNLOCK, 1f, 0.8f);
		hero.sprite.operate(hero.pos);
		updateQuickslot();
		hero.spendAndNext(1f);
	}

	@Override
	public String status() {
		return Integer.toString(rounds);
	}

	@Override
	public String info() {
		String info = super.info();
		info += "\n\n" + Messages.get(this, "ammo", rounds, Magazine.CAPACITY,
				augment.damageFactor(bulletMin()), augment.damageFactor(bulletMax()));
		return info;
	}

	@Override
	public int targetingPos( Hero user, int dst ) {
		return knockBullet().targetingPos(user, dst);
	}

	public Bullet knockBullet(){
		return new Bullet();
	}

	public class Bullet extends MissileWeapon {

		{
			image = ItemSpriteSheet.RIFLE_BULLET;
			hitSound = Assets.Sounds.HIT_ARROW;
			setID = 0;
		}

		@Override
		public ArrayList<String> actions( Hero hero ) {
			return new ArrayList<>();
		}

		@Override
		public String defaultAction() {
			return null;
		}

		@Override
		public int defaultQuantity() {
			return 1;
		}

		@Override
		public int damageRoll( Char owner ) {
			int dmg = Hero.heroDamageIntRange(bulletMin(), bulletMax());
			return augment.damageFactor(dmg);
		}

		@Override
		public boolean hasEnchant( Class<? extends Weapon.Enchantment> type, Char owner ) {
			return ServiceRifle.this.hasEnchant(type, owner);
		}

		@Override
		public int proc( Char attacker, Char defender, int damage ) {
			return ServiceRifle.this.proc(attacker, defender, damage);
		}

		@Override
		public float delayFactor( Char user ) {
			return ServiceRifle.this.delayFactor(user);
		}

		@Override
		public int STRReq( int lvl ) {
			return ServiceRifle.this.STRReq();
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xFFFFD27A, 1 );
			} else if (!curUser.shoot( enemy, this )) {
				Splash.at( cell, 0xFFFFD27A, 1 );
			}
		}

		@Override
		public Item split( int amount ) {
			return null;
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG, 0.7f, Random.Float(1.6f, 1.9f) );
		}

		@Override
		public void cast( final Hero user, final int dst ) {
			rounds = Math.max(0, rounds - 1);
			updateQuickslot();
			super.cast(user, dst);
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				knockBullet().cast(curUser, target);
			}
		}
		@Override
		public String prompt() {
			return Messages.get(ServiceRifle.class, "prompt");
		}
	};

	private static final String ROUNDS = "rounds";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(ROUNDS, rounds);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(ROUNDS)) rounds = bundle.getInt(ROUNDS);
	}
}
