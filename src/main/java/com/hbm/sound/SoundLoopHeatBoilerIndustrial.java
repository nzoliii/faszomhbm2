package com.hbm.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class SoundLoopHeatBoilerIndustrial extends SoundLoopMachine {
    public static List<SoundLoopHeatBoilerIndustrial> list = new ArrayList<SoundLoopHeatBoilerIndustrial>();

    public SoundLoopHeatBoilerIndustrial(SoundEvent path, TileEntity te) {
        super(path, te);
        list.add(this);
        this.attenuationType = ISound.AttenuationType.NONE;
    }

    public TileEntity getTE() {
        return te;
    }
}
