package com.winthier.skills.player;

import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.World;

public class TravellingPlayerInfo {
        public final PlayerInfo info;
        private Location anchor;

        public TravellingPlayerInfo(PlayerInfo info) {
                this.info = info;
        }

        public void setLocation(Location loc) {
                anchor = loc.clone();
        }

        /**
         * @return -1 if it's not in the same world,
         *         else the squared distance
         */
        public int distanceSquared(Location loc) {
                if (loc == null) return -1;
                if (!anchor.getWorld().equals(loc.getWorld())) return -1;
                return Util.horizontalDistanceSquared(anchor, loc);
        }
}
