package net.bzbr.hotwaves.common.business;

import net.bzbr.hotwaves.Hotwaves;
import net.bzbr.hotwaves.common.ai.PlayerTrackingGoal;
import net.bzbr.hotwaves.common.configuration.ConfigManager;
import net.bzbr.hotwaves.common.enums.MobStrength;
import net.bzbr.hotwaves.common.sounds.HordeSounds;
import net.bzbr.hotwaves.mixin.MobEntityMixin;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.entity.EntityType;
import org.slf4j.Logger;

import java.util.*;

public class WaveSpawner {

    private static final Random random = new Random();
    private MinecraftServer server;
    private int currentDayNumber;
    private int currentTick;
    private int currentNoWaveTick;
    private int maxWavesDelay;
    private int spawnIntervalTicks;
    private int spawnRadiusData;
    private int allMobsCount;
    private List<ConfigManager.MobConfig> waveMobsData;
    private boolean isWaveDuring;
    private int killedDuringHorde;
    private ServerWorld world;
    private ArrayList<MobSpawnData> actualMobsData;
    private Logger logger;
    private Random rand;

    private final static Map<Integer, Double> difficultyMap;

    static {
        difficultyMap = new HashMap<>();
        difficultyMap.put(Difficulty.EASY.getId(), 0.3);
        difficultyMap.put(Difficulty.NORMAL.getId(), 0.5);
        difficultyMap.put(Difficulty.HARD.getId(), 0.7);
        difficultyMap.put(Difficulty.PEACEFUL.getId(), 0.0);
    }

    public WaveSpawner(MinecraftServer server, int currentDayNumber) {
        rand = new Random();
        this.logger = Hotwaves.LOGGER;
        this.server = server;
        this.currentDayNumber = currentDayNumber;
        Initialize();
    }

    public void Initialize() {

        spawnRadiusData = ConfigManager.getSpawnRadius();
        var waveMobs = ConfigManager.getWaveMobs();
        waveMobsData = waveMobs.stream().filter(x -> x.minDay() <= this.currentDayNumber && (x.maxDay() >= currentDayNumber || x.maxDay() == -1)).toList();
        allMobsCount = 0;
        currentTick = 0;
        currentNoWaveTick = 0;
        spawnIntervalTicks = 150;
        maxWavesDelay = 600;
    }

    public void StartSpawn() {

        world = server.getOverworld();
        PrepareHordeData();
        isWaveDuring = true;
        DespawnMobsAround();
        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityDeath);
        killedDuringHorde = -1;
        PlayStartSound();
        ServerTickEvents.START_SERVER_TICK.register(this::SpawnTick);
    }

    private void PlayStartSound() {

        final var players = world.getPlayers();
        players.forEach(player -> {
            var basePos = player.getBlockPos();

            BlockPos soundPos;
            soundPos = getClosestLoadedPosFast(world, basePos, getRandomDirection(), 12, 8, 0, false);
            soundPos = new BlockPos(soundPos.getX(), basePos.getY(), soundPos.getZ());
            world.playSound(null, soundPos, HordeSounds.HORDE_START_SOUND_1, SoundCategory.HOSTILE, 0.9f, 1f);
        });
    }

    public void StopSpawn() {

        isWaveDuring = false;
    }

    private void PrepareHordeData() {

        var currentWaveNumber = currentDayNumber / ConfigManager.getWaveIntervalDays();
        double waveMultiplier = Math.max(1, currentWaveNumber * difficultyMap.get(world.getDifficulty().getId()));
        var playersCount = world.getPlayers().size();

        var isSolo = playersCount == 1;

        actualMobsData = new ArrayList<MobSpawnData>();

        waveMobsData.forEach(mob -> {

            var maxCount = 10;

            switch (mob.strength()) {
                case LOW -> {
                    var strengthMobCount = waveMobsData.stream().filter(x -> x.strength() == MobStrength.LOW).count();
                    maxCount = (int) (ConfigManager.getLowStrengthMobMaxCount() / strengthMobCount);
                }
                case MID -> {
                    var strengthMobCount = waveMobsData.stream().filter(x -> x.strength() == MobStrength.MID).count();
                    maxCount = (int) (ConfigManager.getMidStrengthMobMaxCount() / strengthMobCount);
                }
                case HARD -> {
                    var strengthMobCount = waveMobsData.stream().filter(x -> x.strength() == MobStrength.HARD).count();
                    maxCount = (int) (ConfigManager.getHardStrengthMobMaxCount() / strengthMobCount);
                }
            }

            var minCount = ConfigManager.getLowStrengthMobMinCount();
            var actualCount = Math.min((int) (mob.count() * waveMultiplier), maxCount);
            actualCount = Math.max(actualCount, minCount);
            actualCount = isSolo ? (int) (actualCount * 1.3) : actualCount;
            allMobsCount += actualCount;
            var actualMob = new MobSpawnData(mob.mobId(), actualCount);
            actualMobsData.add(actualMob);
            logger.info("Mob type of {} count set to {}", mob.mobId(), actualCount);
        });
    }

    private void SpawnTick(MinecraftServer minecraftServer) {

        if (currentTick >= spawnIntervalTicks) {

            if ((currentNoWaveTick >= maxWavesDelay || killedDuringHorde == -1 || (killedDuringHorde >= (int) (allMobsCount * 0.7))) && isWaveDuring) {
                SpawnMobs();
                currentNoWaveTick = 0;
            }

            currentTick = 0;
        }

        currentNoWaveTick++;
        currentTick++;
    }

    private void SpawnMobs() {

        logger.info("Spawn started");
        killedDuringHorde = 0;
        final var players = world.getPlayers().stream().filter(pl -> !pl.isCreative()).toList();

        if (actualMobsData.stream().count() == 0) {
            PrepareHordeData();
        }
        actualMobsData.forEach(mobSpawnData -> {

            players.forEach(player -> {

                var basePos = player.getBlockPos();

                var spawnPos = getClosestLoadedPosFast(world, basePos, getRandomDirection(), spawnRadiusData, 8, 0, true);

                var posCheck = 0;
                while (spawnPos.equals(basePos)) {

                    spawnPos = getClosestLoadedPosFast(world, basePos, getRandomDirection(), spawnRadiusData, 7, 0, true);
                    if (world.getBlockState(spawnPos).isOf(Blocks.LAVA)) {
                        spawnPos = basePos;
                    }
                    if (posCheck++ >= 5) {
                        spawnPos = getClosestLoadedPosFast(world, basePos, getRandomDirection(), spawnRadiusData, 7, 0, false);
                        break;
                    }
                }

                for (int i = 0; i < mobSpawnData.count; i++) {

                    EntityType<?> mobType = Registries.ENTITY_TYPE.get(new Identifier(mobSpawnData.mobId()));
                    MobEntity mob = (MobEntity) mobType.create(world);
                    if (mob != null) {
                        mob.refreshPositionAndAngles(getSpawnPos(spawnPos), 0, 0);
                        world.spawnEntity(mob);
                        var goalSelector = ((MobEntityMixin) mob).getGoalSelector();
                        var speed = 1 + difficultyMap.get(world.getDifficulty().getId());
                        goalSelector.add(1, new PlayerTrackingGoal(mob, player, speed));
                    }
                }
                logger.info("Mobs are spawned");
            });
        });
    }

    private BlockPos getSpawnPos(BlockPos basepos) {
        for (int j = 0; j < 5; j++) {
            double x = basepos.getX() + rand.nextInt(10);
            double z = basepos.getZ() + rand.nextInt(10);
            var tempPos = new BlockPos((int) x, basepos.getY(), (int) z);
            if (world.getBlockState(tempPos).isOf(Blocks.LAVA))
                return world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, tempPos);
        }
        return basepos;
    }

    public BlockPos getClosestLoadedPosFast(ServerWorld world, BlockPos basePos, Vec3d direction, double radius, int maxLight, int minLight, boolean checkLight) {
        while (radius > 0) {
            // Вычисляем позицию на основе направления и радиуса
            BlockPos targetPos = basePos.add(
                    (int) (direction.x * radius),
                    0,
                    (int) (direction.z * radius)
            );

            // Получаем позицию с учётом высотной карты
            BlockPos heightPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, targetPos);

            // Проверяем, загружен ли чанк
            if (world.isChunkLoaded(heightPos.getX() >> 4, heightPos.getZ() >> 4)) {
                // Проверяем уровень света
                int lightLevel = world.getLightLevel(heightPos);
                if (lightLevel >= (checkLight ? minLight : 0) && lightLevel <= (checkLight ? maxLight : 15)) {
                    return heightPos;
                }
            }

            // Уменьшаем радиус поиска
            radius--;
        }

        // Если ничего не найдено, возвращаем базовую позицию
        return basePos;
    }

    public static BlockPos getClosestLoadedPos(ServerWorld world, BlockPos basePos, Vec3d direction, double radius, int maxLight, int minLight) {
        while (radius > 0) {
            // Вычисляем позицию на основе направления и радиуса
            BlockPos targetPos = basePos.add(
                    (int) (direction.x * radius),
                    0,
                    (int) (direction.z * radius)
            );

            // Получаем позицию с учётом высотной карты
            BlockPos heightPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, targetPos);

            // Проверяем, загружен ли чанк
            if (world.isChunkLoaded(heightPos.getX() >> 4, heightPos.getZ() >> 4)) {
                // Проверяем уровень света
                int lightLevel = world.getLightLevel(heightPos);
                if (lightLevel >= minLight && lightLevel <= maxLight) {
                    return heightPos;
                }
            }

            // Уменьшаем радиус поиска
            radius--;
        }

        // Если ничего не найдено, возвращаем базовую позицию
        return basePos;
    }

    private void onEntityDeath(LivingEntity livingEntity, DamageSource damageSource) {

        killedDuringHorde++;
    }

    private void DespawnMobsAround() {

        var players = world.getPlayers();

        players.forEach(player -> {

            Box boundingBox = new Box(
                    player.getX() - spawnRadiusData, player.getY() - spawnRadiusData, player.getZ() - spawnRadiusData,
                    player.getX() + spawnRadiusData, player.getY() + spawnRadiusData, player.getZ() + spawnRadiusData
            );
            world.getEntitiesByClass(MobEntity.class, boundingBox, (entity) -> {
                // Условие: только враждебные мобы без имени
                return (entity instanceof ZombieEntity // Включает обычных зомби
                        || entity instanceof SkeletonEntity
                        || entity instanceof HuskEntity
                        || entity instanceof DrownedEntity
                        || entity instanceof CreeperEntity) && entity.getCustomName() == null;
            }).forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));

        });
    }

    private Vec3d getRandomDirection() {
        // Генерируем случайный угол в диапазоне от 0 до 2π (азимут)
        double azimuth = random.nextDouble() * 2 * Math.PI;

        // Генерируем случайный угол в диапазоне от 0 до π (зенит)
        double zenith = Math.acos(2 * random.nextDouble() - 1); // Равномерное распределение на сфере

        // Конвертируем сферические координаты в декартовы
        double x = Math.sin(zenith) * Math.cos(azimuth);
        double y = Math.sin(zenith) * Math.sin(azimuth);
        double z = Math.cos(zenith);

        return new Vec3d(x, y, z);
    }

    public record MobSpawnData(String mobId, int count) {
    }
}