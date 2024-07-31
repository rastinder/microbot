/*
 * Copyright (c) 2022, Severi K <severikupari1@gmail.com>
 * Copyright (c) 2019, FlaxOnEm <flax.on.em@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.strongholdofsecurity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.Widget;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
enum SecurityAnswers {
    QUESTION_ANSWER_PAIR_0("To pass you must answer me this: Can I leave my<br>account logged in while I'm out of the room?", "No."),
    QUESTION_ANSWER_PAIR_1("To pass you must answer me this: How do I remove a<br>hijacker from my account?", "Use the Account Recovery System."),
    QUESTION_ANSWER_PAIR_2("To pass you must answer me this: What do you do if<br>someone asks you for your password or bank PIN to<br>make you a player moderator?", "Don't give them the information and send an 'Abuse report'."),
    QUESTION_ANSWER_PAIR_3("To pass you must answer me this: My friend asks me<br>for my password so that he can do a difficult quest for<br>me. Do I give it to them?", "Don't give them my password."),
    QUESTION_ANSWER_PAIR_4("To pass you must answer me this: Who can I give my<br>password to?", "Nobody."),
    QUESTION_ANSWER_PAIR_5("To pass you must answer me this: How do I set up<br>two-factor authentication for my Old School RuneScape<br>account?", "Through account settings on oldschool.runescape.com."),
    QUESTION_ANSWER_PAIR_6("To pass you must answer me this: Hey adventurer!<br>You've been randomly selected for a prize of 1 year of<br>free membership! I'm just going to need some of your<br>account details so I can put it on your account!", "No way! I'm reporting you to Jagex!"),
    QUESTION_ANSWER_PAIR_7("To pass you must answer me this: What is an example<br>of a good bank PIN?", "The birthday of a famous person or event."),
    QUESTION_ANSWER_PAIR_8("To pass you must answer me this: You're watching a<br>stream by someone claiming to be Jagex offering double<br>xp. What do you do?", "Report the stream as a scam. Real Jagex streams have a 'verified' mark."),
    QUESTION_ANSWER_PAIR_9("To pass you must answer me this: What should you do<br>if your real-life friend asks for your password so he<br>can check your stats?", "Don't give out your password to anyone. Not even close friends."),
    QUESTION_ANSWER_PAIR_10("To pass you must answer me this: A player tells you to<br>search for a video online, click the link in the description<br>and comment on the forum post to win a cash prize.<br>What do you do?", "Report the player for phishing."),
    QUESTION_ANSWER_PAIR_11("To pass you must answer me this: Will Jagex prevent<br>me from saying my PIN in game?", "No."),
    QUESTION_ANSWER_PAIR_12("To pass you must answer me this: You have been<br>offered an opportunity to check out a free giveaway or<br>double XP signup via email or in game chat. What<br>should I do?", "Report the incident and do not click any links."),
    QUESTION_ANSWER_PAIR_13("that sound?", "No way! You'll just take my gold for your own! Reported!"),
    QUESTION_ANSWER_PAIR_14("To pass you must answer me this: A website claims that<br>they can make me a player moderator. What should I<br>do?", "Inform Jagex by emailing reportphishing@jagex.com."),
    QUESTION_ANSWER_PAIR_15("To pass you must answer me this: How do I set a<br>bank PIN?", "Talk to any banker."),
    QUESTION_ANSWER_PAIR_16("react?", "Don't share your information and report the player."),
    QUESTION_ANSWER_PAIR_17("To pass you must answer me this: You are part way<br>through the Stronghold of Security when you have to<br>answer another question. After you answer the question,<br>you should...", "Read the text and follow the advice given."),
    QUESTION_ANSWER_PAIR_18("To pass you must answer me this: What should I do if<br>I receive an email asking me to verify my identity or<br>Account details due to suspicious activity?", "Don't click any links, forward the email to reportphishing@jagex.com."),
    QUESTION_ANSWER_PAIR_19("To pass you must answer me this: What do you do if<br>someone asks you for your password or bank PIN to<br>make you a member for free?", "Don't tell them anything and click the 'Report Abuse' button."),
    QUESTION_ANSWER_PAIR_20("To pass you must answer me this: What do I do if a<br>moderator asks me for my account details?", "Politely tell them no and then use the 'Report Abuse' button."),
    QUESTION_ANSWER_PAIR_21("To pass you must answer me this: Is it OK to buy an<br>Old School RuneScape account?", "No, you should never buy an account."),
    QUESTION_ANSWER_PAIR_22("To pass you must answer me this: Who is it ok to<br>share my account with?", "Nobody."),
    QUESTION_ANSWER_PAIR_23("To pass you must answer me this: You have been<br>offered an opportunity to check out a free giveaway or<br>double XP signup via social media or stream. What<br>should I do?", "Report the incident and do not click any links."),
    QUESTION_ANSWER_PAIR_24("To pass you must answer me this: Where is it safe to<br>use my Old School RuneScape password?", "Only on the Old School RuneScape website."),
    QUESTION_ANSWER_PAIR_25("To pass you must answer me this: What should you do<br>if another player messages you recommending a website<br>to purchase items and/or gold?", "Do not visit the website and report the player who messaged you."),
    QUESTION_ANSWER_PAIR_26("To pass you must answer me this: Whose responsibility<br>is it to keep your account secure?", "Me."),
    QUESTION_ANSWER_PAIR_27("To pass you must answer me this: Is it safe to pay<br>someone to level your account?", "No, you should never allow anyone to level your account."),
    QUESTION_ANSWER_PAIR_28("To pass you must answer me this: What is the best<br>security step you can take to keep your registered<br>email secure?", "Set up 2 step authentication with my email provider."),
    QUESTION_ANSWER_PAIR_29("To pass you must answer me this: What is the best<br>way to secure your account?", "Authenticator and two-step login on my registered email."),
    QUESTION_ANSWER_PAIR_30("To pass you must answer me this: What do I do if my<br>account is compromised?", "Secure my device and reset my password."),
    QUESTION_ANSWER_PAIR_31("respond?", "Decline the offer and report that player."),
    QUESTION_ANSWER_PAIR_32("To pass you must answer me this: What do I do if I<br>think I have a keylogger or virus?", "Virus scan my device then change my password."),
    QUESTION_ANSWER_PAIR_33("To pass you must answer me this: A player says that<br>Jagex prevents you from saying your password<br>backwards in game. What do you do?", "Don't type in my password backwards and report the player.");

    private final String question;
    private final String answer;
    static final Map<String, String> QUESTION_ANSWER_MAP = new HashMap<>();

    static {
        Arrays.stream(SecurityAnswers.values())
                .forEach(securityAnswers -> QUESTION_ANSWER_MAP.put(
                                securityAnswers.question,
                                securityAnswers.answer
                        )
                );
    }

    static Widget findMatchingWidgetForQuestion(final String question, final Widget[] widgets) {
        return getWidgetForAnswer(widgets, QUESTION_ANSWER_MAP.get(question));
    }

    private static Widget getWidgetForAnswer(Widget[] widgets, String answer) {
        for (Widget widget : widgets) {
            if (widget != null && widget.getText().equals(answer)) {
                return widget;
            }
        }
        return null;
    }
}
