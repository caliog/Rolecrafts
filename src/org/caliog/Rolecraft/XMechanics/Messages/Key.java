package org.caliog.Rolecraft.XMechanics.Messages;

public enum Key {

	// @formatter:off
	// GENERAL
	DEAD_MESSAGE,
	LEVEL_REACHED,
	//CLASS
	CLASS_CHANGED,
	CLASS_CHANGE_OFFER,
	//SKILLS
	FULL_STR,
	FULL_VIT,
	FULL_INT,
	FULL_DEX,
	SKILL_NEED_LEVEL,
	SKILL_NEED_MANA,
	SKILL_ACTIVE,
	//ITEMS
	NEED_CLASS1,
	NEED_CLASS2,
	NEED_EXP1,
	NEED_EXP2,
	WEAPON_LEVEL,
	//GROUP
	GROUP_CREATED,
	GROUP_CREATE_FAIL,
	GROUP_LEFT,
	GROUP_NOT_A_MEMBER,
	GROUP_FAIL,
	GROUP_JOIN_FAIL,
	GROUP_INVITED,
	GROUP_CANNOT_KICK_PLAYER, 
	// quest
	QUEST_TARGET_VILLAGER, 
	QUEST_COMPLETED, 
	QUEST_INFO_MOBS,
	QUEST_INFO_COLLECT, 
	QUEST_INFO_REWARD, 
	QUEST_INFO_START_ITEMS, 
	QUEST_MISSING_COLLECT, 
	QUEST_DELIVERED_ITEMS, 
	// spells
	SPELL_CLICK_POWER,
	SPELL_NO_POWER,
	// words
	WORD_AMOUNT,
	WORD_MOB,
	WORD_REQUIRED_CLASS,
	WORD_MINIMUM_LEVEL,
	WORD_EXPERIENCE,
	WORD_ACCEPT,
	WORD_BACK,
	WORD_ITEMS,
	WORD_START,
	WORD_REWARD,
	WORD_COLLECT,
	WORD_WANTED_MOBS,
	WORD_LEFT,
	WORD_RIGHT,
	WORD_SOULBOUND,
	WORD_CLASS,
	WORD_COSTS, 
	WORD_VALUE;
	// @formatter:on

	public String getKey() {
		return this.name().toLowerCase().replaceAll("_", "-");
	}

	public String getString() {
		return Msg.file.getString(getKey());
	}

}
