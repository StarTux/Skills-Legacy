package com.winthier.skills.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton;

public abstract class SkillsEntity {
        public abstract EntityType getEntityType();
        public abstract boolean matches(Entity e);

        private static SkillsEntity fromStringBukkit(String string) {
                final EntityType entityType = EntityType.fromName(string);
                if (entityType == null) return null;
                return new SkillsEntity() {
                        public EntityType getEntityType() {
                                return entityType;
                        }
                        public boolean matches(Entity e) {
                                return e.getType() == entityType;
                        }
                };
        }

        private static SkillsEntity fromStringCustom(String string) {
                if (string.equalsIgnoreCase("Skeleton")) {
                        return new SkillsEntity() {
                                public EntityType getEntityType() {
                                        return EntityType.SKELETON;
                                }
                                public boolean matches(Entity e) {
                                        return ((Skeleton)e).getSkeletonType() == Skeleton.SkeletonType.NORMAL;
                                }
                        };
                }
                if (string.equalsIgnoreCase("WitherSkeleton")) {
                        return new SkillsEntity() {
                                public EntityType getEntityType() {
                                        return EntityType.SKELETON;
                                }
                                public boolean matches(Entity e) {
                                        return ((Skeleton)e).getSkeletonType() == Skeleton.SkeletonType.WITHER;
                                }
                        };
                }
                return null;
        }

        public static SkillsEntity fromString(String string) {
                SkillsEntity result = null;
                if (result == null) result = fromStringBukkit(string);
                if (result == null) {
                        System.err.println("[Skills] Invalid entity descriptor: " + string);
                }
                return result;
        }
}
