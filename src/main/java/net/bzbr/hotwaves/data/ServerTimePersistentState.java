package net.bzbr.hotwaves.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class ServerTimePersistentState extends PersistentState {

    private int dayNumber;
    private boolean isDay;
    private boolean isWaveRunning;

    public ServerTimePersistentState(){

        this.dayNumber = 0;
        this.isDay = true;
    }

    public void setIsDay(boolean isDay) {

        this.isDay = isDay;
        this.markDirty();
    }

    public void setDayNumber(int day) {

        this.dayNumber = day;
        this.markDirty();
    }

    public void setIsWaveRunning(boolean isWaveRunning) {

        this.isWaveRunning = isWaveRunning;
        this.markDirty();
    }

    public void incrementDayNumber() {

        this.dayNumber++;
        this.markDirty();
    }

    public int getDayNumber() {

        return this.dayNumber;
    }

    public boolean getIsDay() {

        return this.isDay;
    }

    public boolean getIsWaveRunning() {

        return this.isWaveRunning;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        nbt.putInt("DayNumber", this.dayNumber);
        nbt.putBoolean("IsDay", this.isDay);
        nbt.putBoolean("IsWaveRunning", this.isWaveRunning);
        return nbt;
    }

    public static ServerTimePersistentState fromNbt(NbtCompound nbt) {

        var data = new ServerTimePersistentState();
        data.dayNumber = nbt.getInt("DayNumber");
        data.isDay = nbt.getBoolean("IsDay");
        data.isWaveRunning = nbt.getBoolean("IsWaveRunning");
        return data;
    }
}
