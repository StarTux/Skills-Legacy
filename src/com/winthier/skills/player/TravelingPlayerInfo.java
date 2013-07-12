package com.winthier.skills.player;

import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.World;

public class TravelingPlayerInfo {
        public final PlayerInfo info;
        private Location anchor, farTravelAnchor;

        public TravelingPlayerInfo(PlayerInfo info) {
                this.info = info;
        }

        public void setLocation(Location loc) {
                anchor = loc.clone();
        }

        public void setFarTravelLocation(Location loc) {
                farTravelAnchor = loc.clone();
        }

        /**
         * @return -1 if it's not in the same world,
         *         else the squared distance
         */
        public int distanceSquared(Location loc) {
                if (loc == null) return -1;
                if (anchor == null || !anchor.getWorld().equals(loc.getWorld())) {
                        setLocation(loc);
                        return -1;
                }
                return Util.horizontalDistanceSquared(anchor, loc);
        }

        public int farTravelDistanceSquared(Location loc) {
                if (loc == null) return -1;
                if (farTravelAnchor == null || !farTravelAnchor.getWorld().equals(loc.getWorld())) {
                        setLocation(loc);
                        return -1;
                }
                return Util.horizontalDistanceSquared(farTravelAnchor, loc);
        }
}
