#!/usr/bin/env python3
"""Create original, deterministic entity sound effects for The Outlands.

Requires Python 3 and ffmpeg. The script writes mono OGG Vorbis files directly
into the mod resource tree so CI packages the same sounds as local builds.
"""
import math
import os
import random
import shutil
import struct
import subprocess
import tempfile
import wave

RATE = 22050
OUT = os.path.join("src", "main", "resources", "assets", "theoutlands", "sounds")
RNG = random.Random(20261120)

def clamp(x):
    return max(-1.0, min(1.0, x))

def make(name, duration, sample_fn):
    count = int(RATE * duration)
    os.makedirs(OUT, exist_ok=True)
    fd, wav_path = tempfile.mkstemp(suffix=".wav")
    os.close(fd)
    ogg_path = os.path.join(OUT, name + ".ogg")
    try:
        with wave.open(wav_path, "wb") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            frames = bytearray()
            for i in range(count):
                t = i / RATE
                value = clamp(sample_fn(t, duration, i, count))
                frames.extend(struct.pack("<h", int(value * 26000)))
            wav.writeframes(frames)
        subprocess.run([
            "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
            "-i", wav_path, "-ac", "1", "-ar", str(RATE),
            "-c:a", "libvorbis", "-q:a", "2", ogg_path
        ], check=True)
    finally:
        if os.path.exists(wav_path):
            os.remove(wav_path)

def noise(t, low, high, state):
    # Differenced deterministic noise with a controllable tonal center.
    raw = RNG.uniform(-1.0, 1.0)
    state["lp1"] += (raw - state["lp1"]) * min(1.0, high / RATE * 6.0)
    state["lp2"] += (raw - state["lp2"]) * min(1.0, low / RATE * 6.0)
    return state["lp1"] - state["lp2"]

def fade(t, duration, attack=0.04, release=0.2):
    return min(1.0, t / attack) * min(1.0, max(0.0, (duration - t) / release))

# Cave.jar: deep hollow resonance under wet, irregular scraping.
def cave(t, d, i, n):
    e = fade(t, d, .12, .3)
    low = math.sin(2 * math.pi * (42 - 9*t/d) * t)
    hollow = math.sin(2 * math.pi * 79 * t + 0.7*math.sin(2*math.pi*2.2*t))
    rasp = noise(t, 110, 760, cave.state)
    grit = noise(t, 850, 3000, cave.grit)
    pulse = max(0.0, math.sin(2*math.pi*3.1*t + .5)) ** 5
    return e * (.32*low + .18*hollow + .19*rasp + .12*grit*pulse)
cave.state, cave.grit = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("cavejar_stalk", 2.1, cave)

# Stalker: intimate breath pulses, airy inhale and rough exhale.
def stalker(t, d, i, n):
    phase = 2*math.pi*1.12*t + .35
    inhale = max(0.0, math.sin(phase)) ** 2
    exhale = max(0.0, math.sin(phase + 1.7)) ** 3
    a = noise(t, 170, 1100, stalker.a)
    b = noise(t, 950, 3900, stalker.b)
    return fade(t,d,.05,.22) * (.38*a*(.15+inhale) + .15*b*exhale + .035*math.sin(2*math.pi*58*t))
stalker.a, stalker.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("stalker_breath", 1.85, stalker)

# Observer: three isolated resonant clicks with short, brittle tails.
def observer(t, d, i, n):
    result = 0.0
    for at, freq, amp in ((.11, 1760, .68), (.43, 2380, .43), (.91, 1510, .55)):
        x = t-at
        if 0 <= x < .15:
            result += amp * (math.sin(2*math.pi*freq*x)*math.exp(-x*39)
                             + .25*math.sin(2*math.pi*freq*1.71*x)*math.exp(-x*63))
    return result
make("observer_click", 1.2, observer)

# Stillborn: heavy stone grinding, intermittent brittle fractures.
def stillborn(t, d, i, n):
    slow = max(0.0, math.sin(2*math.pi*.72*t)) ** 1.7
    a = noise(t, 48, 420, stillborn.a)
    b = noise(t, 480, 1850, stillborn.b)
    ticks = 0.0
    for at in (.48, 1.21, 1.55):
        x=t-at
        if 0 <= x < .05:
            ticks += .27*math.sin(2*math.pi*710*x)*math.exp(-x*95)
    return fade(t,d,.08,.22) * (.37*a*slow + .19*b*slow + .14*math.sin(2*math.pi*31*t) + ticks)
stillborn.a, stillborn.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("stillborn_stone", 2.0, stillborn)

# Stillborn strike: compact bass impact followed by gritty stone fragments.
def stillborn_hit(t, d, i, n):
    a = noise(t, 60, 1000, stillborn_hit.a)
    b = noise(t, 1000, 6000, stillborn_hit.b)
    return .58*a*math.exp(-t*13) + .25*math.sin(2*math.pi*(72-24*t)*t)*math.exp(-t*9) + .13*b*math.exp(-t*32)
stillborn_hit.a, stillborn_hit.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("stillborn_strike", .9, stillborn_hit)

# Agitated One warning: rising dissonant growl with an accelerating pulse.
def warning(t, d, i, n):
    rise=t/d
    freq=66+94*rise**1.7
    phase=2*math.pi*(66*t + 47*t*t/d)
    pulse=max(0.0, math.sin(2*math.pi*(3.0+2.1*rise)*t))**5
    a=noise(t,180,1500,warning.a)
    b=noise(t,1400,4400,warning.b)
    return fade(t,d,.1,.17) * (.32*math.sin(phase)+.2*math.sin(phase*1.51+.3)+.22*a*pulse+.09*b*rise)
warning.a, warning.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("agitated_warning", 1.55, warning)

# Agitated One charge: abrupt transient into a rushing, distorted roar.
def charge(t, d, i, n):
    a=noise(t,80,1250,charge.a)
    b=noise(t,900,6500,charge.b)
    hit=0.0
    if t < .035:
        hit=.42*noise(t,70,6500,charge.hit)
    return hit + .4*a*math.exp(-t*2.7) + .27*math.sin(2*math.pi*(54+31*t)*t)*math.exp(-t*3.2) + .2*b*math.exp(-t*5)
charge.a, charge.b, charge.hit = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("agitated_charge", 1.0, charge)

print("Generated seven original entity sound effects in", OUT)
