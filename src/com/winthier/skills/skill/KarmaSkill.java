package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.voterecord.VoteRecordPlugin;
import com.winthier.voterecord.api.IntArrayCallback;
import com.winthier.voterecord.api.Vote;
import com.winthier.voterecord.event.VoteEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class KarmaSkill extends AbstractSkill {
        private WeakReference<VoteRecordPlugin> voteRecordPlugin = null;
        private int baseXp = 1;

        public KarmaSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        public VoteRecordPlugin getVoteRecordPlugin() {
                if (voteRecordPlugin != null) {
                        VoteRecordPlugin result = voteRecordPlugin.get();
                        if (result != null) return result;
                }
                final Plugin tmp = plugin.getServer().getPluginManager().getPlugin("VoteRecord");
                if (tmp == null || !(tmp instanceof VoteRecordPlugin)) return null;
                VoteRecordPlugin result = (VoteRecordPlugin)tmp;
                voteRecordPlugin = new WeakReference<VoteRecordPlugin>(result);
                return result;
        }

        @EventHandler
        public void onVote(VoteEvent event) {
                final Vote vote = event.getVote();
                VoteRecordPlugin plugin = getVoteRecordPlugin();
                if (plugin == null) {
                        plugin.getLogger().warning("VoteRecordPlugin not found!");
                        return;
                }
                plugin.sqlManager.countDailyVotes(vote.username, new Date(), 10, new IntArrayCallback() {
                        public void callback(int counts[]) {
                                voted(vote, counts);
                        }
                });
        }

        public void voted(Vote vote, int counts[]) {
                // Count how many consecutive past days have been with at least one vote.
                int i = 0;
                for (; i < counts.length && counts[i] > 0; ++i);
                // Since we just received a vote, the event of i == 0 would surprise me.

                // Give SP. Player might not be online.
                final int sp = i;
                plugin.playerManager.addSkillPoints(vote.username, skillType, sp);

                // Give XP.
                if (plugin.perksEnabled) {
                        final Player player = plugin.getServer().getPlayerExact(vote.username);
                        if (player != null) {
                                player.giveExp(multiplyXp(player, baseXp));
                        }
                }
        }

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Get " + multiplyXp(player, baseXp) + " XP per vote");
                return result;
        }

        @Override
        public void loadConfiguration() {
                baseXp = getConfig().getInt("BaseExp");
        }
}
