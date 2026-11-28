#!/usr/bin/env python3
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
                value = sample_fn(t, duration, i, count)

                if not name.startswith("structure_"):
                    pulse = 0.42 + 0.58 * (0.5 + 0.5 * math.sin(2 * math.pi * 0.73 * t + 0.4))
                    envelope = min(1.0, t / 0.07) * min(1.0, max(0.0, (duration - t) / 0.24))
                    warped = 38.0 + 3.2 * math.sin(2 * math.pi * 0.31 * t)
                    undertone = math.sin(2 * math.pi * warped * t + 0.22 * math.sin(2 * math.pi * 1.1 * t))
                    throat = math.sin(2 * math.pi * 76.0 * t + 0.35 * math.sin(2 * math.pi * 0.57 * t))
                    value += envelope * pulse * (0.052 * undertone + 0.018 * throat)
                value = clamp(value)
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

    raw = RNG.uniform(-1.0, 1.0)
    state["lp1"] += (raw - state["lp1"]) * min(1.0, high / RATE * 6.0)
    state["lp2"] += (raw - state["lp2"]) * min(1.0, low / RATE * 6.0)
    return state["lp1"] - state["lp2"]

def fade(t, duration, attack=0.04, release=0.2):
    return min(1.0, t / attack) * min(1.0, max(0.0, (duration - t) / release))

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

def stalker(t, d, i, n):
    phase = 2*math.pi*1.12*t + .35
    inhale = max(0.0, math.sin(phase)) ** 2
    exhale = max(0.0, math.sin(phase + 1.7)) ** 3
    a = noise(t, 170, 1100, stalker.a)
    b = noise(t, 950, 3900, stalker.b)
    return fade(t,d,.05,.22) * (.38*a*(.15+inhale) + .15*b*exhale + .035*math.sin(2*math.pi*58*t))
stalker.a, stalker.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("stalker_breath", 1.85, stalker)

def observer(t, d, i, n):
    result = 0.0
    for at, freq, amp in ((.11, 1760, .68), (.43, 2380, .43), (.91, 1510, .55)):
        x = t-at
        if 0 <= x < .15:
            result += amp * (math.sin(2*math.pi*freq*x)*math.exp(-x*39)
                             + .25*math.sin(2*math.pi*freq*1.71*x)*math.exp(-x*63))
    return result
make("observer_click", 1.2, observer)

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

def stillborn_hit(t, d, i, n):
    a = noise(t, 60, 1000, stillborn_hit.a)
    b = noise(t, 1000, 6000, stillborn_hit.b)
    return .58*a*math.exp(-t*13) + .25*math.sin(2*math.pi*(72-24*t)*t)*math.exp(-t*9) + .13*b*math.exp(-t*32)
stillborn_hit.a, stillborn_hit.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("stillborn_strike", .9, stillborn_hit)

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

def charge(t, d, i, n):
    a=noise(t,80,1250,charge.a)
    b=noise(t,900,6500,charge.b)
    hit=0.0
    if t < .035:
        hit=.42*noise(t,70,6500,charge.hit)
    return hit + .4*a*math.exp(-t*2.7) + .27*math.sin(2*math.pi*(54+31*t)*t)*math.exp(-t*3.2) + .2*b*math.exp(-t*5)
charge.a, charge.b, charge.hit = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("agitated_charge", 1.0, charge)

def lurker_breath(t, d, i, n):
    s = lurker_breath.state
    air = noise(t, 180, 1450, s)
    rasp = noise(t, 900, 3600, lurker_breath.grit)
    pulse = max(0.0, math.sin(2 * math.pi * 0.72 * t + .3)) ** 2
    return fade(t, d, .08, .24) * (.31 * air * (.2 + pulse) + .13 * rasp * pulse + .045 * math.sin(2 * math.pi * 49 * t))
lurker_breath.state, lurker_breath.grit = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("lurker_breath", 2.0, lurker_breath)

def lurker_hit(t, d, i, n):
    a = noise(t, 90, 900, lurker_hit.a)
    b = noise(t, 1200, 5200, lurker_hit.b)
    return .44 * a * math.exp(-t * 16) + .18 * b * math.exp(-t * 37) + .2 * math.sin(2 * math.pi * 63 * t) * math.exp(-t * 11)
lurker_hit.a, lurker_hit.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("lurker_hit", .72, lurker_hit)

def howler_call(t, d, i, n):
    p = t / d
    f = 72 + 112 * p
    phase = 2 * math.pi * (72 * t + 56 * t * t / d)
    a = noise(t, 130, 1000, howler_call.a)
    b = noise(t, 700, 2600, howler_call.b)
    wobble = math.sin(2 * math.pi * (2.1 + p) * t)
    return fade(t, d, .16, .52) * (.34 * math.sin(phase + wobble) + .21 * math.sin(phase * 1.49) + .2 * a + .1 * b)
howler_call.a, howler_call.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("howler_call", 3.1, howler_call)

def howler_breath(t, d, i, n):
    a = noise(t, 100, 720, howler_breath.a)
    b = noise(t, 600, 2100, howler_breath.b)
    gate = max(0.0, math.sin(2 * math.pi * .55 * t)) ** 2
    return fade(t, d, .1, .2) * (.3 * a * gate + .16 * b * gate)
howler_breath.a, howler_breath.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("howler_breath", 2.4, howler_breath)

def howler_hit(t, d, i, n):
    a = noise(t, 60, 1200, howler_hit.a)
    b = noise(t, 900, 4300, howler_hit.b)
    return .48 * a * math.exp(-t * 12) + .18 * b * math.exp(-t * 31)
howler_hit.a, howler_hit.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("howler_hit", .8, howler_hit)

def dredge_crawl(t, d, i, n):
    a = noise(t, 65, 500, dredge_crawl.a)
    b = noise(t, 500, 2200, dredge_crawl.b)
    grit = max(0.0, math.sin(2 * math.pi * 4.2 * t + .5)) ** 7
    return fade(t, d, .04, .18) * (.32 * a + .2 * b * grit + .1 * math.sin(2 * math.pi * 37 * t))
dredge_crawl.a, dredge_crawl.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("dredge_crawl", 1.8, dredge_crawl)

def dredge_lunge(t, d, i, n):
    a = noise(t, 90, 1500, dredge_lunge.a)
    b = noise(t, 1100, 6200, dredge_lunge.b)
    sweep = math.sin(2 * math.pi * (160 * t - 75 * t * t))
    return .32 * a * math.exp(-t * 3.8) + .2 * b * math.exp(-t * 8) + .12 * sweep * math.exp(-t * 5)
dredge_lunge.a, dredge_lunge.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("dredge_lunge", 1.05, dredge_lunge)

def dredge_hit(t, d, i, n):
    a = noise(t, 50, 800, dredge_hit.a)
    b = noise(t, 800, 4600, dredge_hit.b)
    return .5 * a * math.exp(-t * 15) + .22 * b * math.exp(-t * 34) + .12 * math.sin(2 * math.pi * 51 * t) * math.exp(-t * 9)
dredge_hit.a, dredge_hit.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("dredge_hit", .7, dredge_hit)

def apostle_whisper(t, d, i, n):
    a = noise(t, 210, 980, apostle_whisper.a)
    b = noise(t, 1250, 4700, apostle_whisper.b)
    syllable = max(0.0, math.sin(2 * math.pi * .82 * t + .2)) ** 2
    return fade(t, d, .18, .32) * (.24 * a * syllable + .12 * b * syllable
                                    + .025 * math.sin(2 * math.pi * 118 * t))
apostle_whisper.a, apostle_whisper.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("null_apostle_whisper", 2.25, apostle_whisper)

def fear_presence(t, d, i, n):
    a = noise(t, 110, 820, fear_presence.a)
    b = noise(t, 900, 3600, fear_presence.b)
    beat = math.sin(2 * math.pi * .31 * t)
    tone = math.sin(2 * math.pi * (218 + 3 * beat) * t)
    return fade(t, d, .45, .75) * (.16 * tone + .15 * a + .07 * b + .05 * math.sin(2*math.pi*109*t))
fear_presence.a, fear_presence.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("fear_presence", 3.2, fear_presence)

def death_presence(t, d, i, n):
    a = noise(t, 35, 360, death_presence.a)
    b = noise(t, 420, 1500, death_presence.b)
    pulse = max(0.0, math.sin(2 * math.pi * .48 * t + .4)) ** 3
    return fade(t, d, .25, .55) * (.32 * a * (.3 + pulse) + .09 * b * pulse
                                    + .11 * math.sin(2*math.pi*(32-2*t)*t))
death_presence.a, death_presence.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("death_presence", 2.8, death_presence)

def almighty_presence(t, d, i, n):
    a = noise(t, 45, 520, almighty_presence.a)
    b = noise(t, 300, 1700, almighty_presence.b)
    swell = .35 + .65 * (0.5 + .5 * math.sin(2 * math.pi * .22 * t))
    chord = (math.sin(2*math.pi*43*t) + .55*math.sin(2*math.pi*64.5*t)
             + .28*math.sin(2*math.pi*86.4*t))
    return fade(t, d, .35, .7) * swell * (.22 * chord + .16 * a + .07 * b)
almighty_presence.a, almighty_presence.b = {"lp1":0.0,"lp2":0.0}, {"lp1":0.0,"lp2":0.0}
make("almighty_presence", 3.6, almighty_presence)

def structure_tone(name, duration, base, noise_amount, pulse_rate):
    state_a = {"lp1": 0.0, "lp2": 0.0}
    state_b = {"lp1": 0.0, "lp2": 0.0}
    def sample(t, d, i, n):
        slow = math.sin(2 * math.pi * (0.13 + pulse_rate * 0.12) * t)
        sweep = base * (1.0 + 0.08 * math.sin(2 * math.pi * 0.19 * t))
        fundamental = math.sin(2 * math.pi * sweep * t + 0.7 * slow)
        harmonic = math.sin(2 * math.pi * (base * (1.47 + 0.025 * slow)) * t)
        low = noise(t, max(25, base * 0.55), max(180, base * 4.2), state_a)
        high = noise(t, max(220, base * 2.0), min(7000, base * 12.0), state_b)
        pulse = 0.58 + 0.42 * math.sin(2 * math.pi * pulse_rate * t + 0.4)
        envelope = fade(t, d, min(0.55, d * 0.18), min(0.75, d * 0.24))
        return envelope * (0.22 * fundamental + 0.11 * harmonic
                           + noise_amount * low * pulse + noise_amount * 0.28 * high
                           + 0.035 * math.sin(2 * math.pi * (base * 0.5) * t))
    make(name, duration, sample)

structure_tone("structure_cabin", 1.7, 118, 0.24, 0.8)
structure_tone("structure_watchtower", 2.5, 72, 0.32, 0.45)
structure_tone("structure_bunker", 1.5, 58, 0.28, 2.8)
structure_tone("structure_shrine", 2.4, 214, 0.13, 0.55)
structure_tone("structure_observatory", 2.8, 440, 0.12, 0.32)
structure_tone("structure_death_archive", 2, 84, 0.22, 1.1)
structure_tone("structure_research_facility", 2.1, 46, 0.3, 3.4)
structure_tone("structure_apostle_cathedral", 3, 55, 0.18, 0.22)
structure_tone("structure_forsaken_settlement", 2.3, 92, 0.34, 0.7)
structure_tone("structure_last_outpost", 1.8, 68, 0.3, 2.1)
structure_tone("structure_forgotten_station", 2.6, 102, 0.25, 1.7)
structure_tone("structure_null_chamber", 3.2, 31, 0.08, 0.18)
structure_tone("structure_hollow_crypt", 2.7, 39, 0.26, 0.4)
structure_tone("structure_boundary_monument", 3, 64, 0.17, 0.26)

print("Generated original Outlands entity and structure sound effects in", OUT)

def pilgrim_breath(t,d,i,n):
 a=noise(t,90,520,pilgrim_breath.a);b=noise(t,620,2100,pilgrim_breath.b);p=max(0,math.sin(2*math.pi*.38*t+.4))**2
 return fade(t,d,.2,.45)*(.28*a*p+.09*b*p+.035*math.sin(2*math.pi*58*t))
pilgrim_breath.a,pilgrim_breath.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("pilgrim_breath",2.7,pilgrim_breath)
def pilgrim_hurt(t,d,i,n): return .38*noise(t,80,850,pilgrim_hurt.a)*math.exp(-t*10)+.16*noise(t,700,3200,pilgrim_hurt.b)*math.exp(-t*24)
pilgrim_hurt.a,pilgrim_hurt.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("pilgrim_hurt",.9,pilgrim_hurt)
def ceiling_crawl(t,d,i,n):
 a=noise(t,150,1100,ceiling_crawl.a);b=noise(t,900,4700,ceiling_crawl.b);return fade(t,d,.03,.18)*(.25*a+.14*b+.12*math.sin(2*math.pi*520*t)*math.exp(-t*2))
ceiling_crawl.a,ceiling_crawl.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("ceiling_crawl",1.5,ceiling_crawl)
def ceiling_hit(t,d,i,n): return .48*noise(t,65,900,ceiling_hit.a)*math.exp(-t*14)+.2*noise(t,1100,5500,ceiling_hit.b)*math.exp(-t*30)
ceiling_hit.a,ceiling_hit.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("ceiling_hit",.72,ceiling_hit)
def colossus_step(t,d,i,n):
 a=noise(t,28,180,colossus_step.a);b=noise(t,180,650,colossus_step.b);return fade(t,d,.02,.65)*(.38*a*math.exp(-t*1.7)+.18*b*math.exp(-t*4)+.26*math.sin(2*math.pi*(38-9*t)*t)*math.exp(-t*2.7))
colossus_step.a,colossus_step.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("colossus_step",.72,colossus_step)
def colossus_hurt(t,d,i,n): return .42*noise(t,25,260,colossus_hurt.a)*math.exp(-t*4)+.2*noise(t,240,1200,colossus_hurt.b)*math.exp(-t*11)
colossus_hurt.a,colossus_hurt.b={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.};make("colossus_hurt",1.7,colossus_hurt)

def pilgrim_step(t,d,i,n):
    low=noise(t,45,260,pilgrim_step.low); grit=noise(t,280,1800,pilgrim_step.grit)
    return fade(t,d,.002,.11)*(.34*low*math.exp(-t*20)+.16*grit*math.exp(-t*30))
pilgrim_step.low,pilgrim_step.grit={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.}
make("pilgrim_step",.16,pilgrim_step)

def ceiling_drop(t,d,i,n):
    thud=noise(t,45,300,ceiling_drop.thud); scrape=noise(t,300,2100,ceiling_drop.scrape)
    return fade(t,d,.001,.2)*(.52*thud*math.exp(-t*13)+.19*scrape*math.exp(-t*8))
ceiling_drop.thud,ceiling_drop.scrape={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.}
make("ceiling_drop",.55,ceiling_drop)

def colossus_warning(t,d,i,n):
    low=noise(t,22,150,colossus_warning.low); upper=noise(t,150,520,colossus_warning.upper)
    swell=math.sin(math.pi*min(1.0,t/d))**1.5
    return fade(t,d,.15,.2)*(.32*low+.12*upper+.22*math.sin(2*math.pi*(31-8*t)*t))*swell
colossus_warning.low,colossus_warning.upper={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.}
make("colossus_warning",1.35,colossus_warning)

def colossus_stomp(t,d,i,n):
    low=noise(t,18,130,colossus_stomp.low); crack=noise(t,130,900,colossus_stomp.crack)
    return fade(t,d,.001,.48)*(.56*low*math.exp(-t*5)+.24*crack*math.exp(-t*11)+.13*math.sin(2*math.pi*42*t)*math.exp(-t*9))
colossus_stomp.low,colossus_stomp.crack={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.}
make("colossus_stomp",.9,colossus_stomp)

def colossus_rumble(t,d,i,n):
    low=noise(t,18,125,colossus_rumble.low); mid=noise(t,125,480,colossus_rumble.mid)
    pulse=.5+.5*math.sin(2*math.pi*.72*t)
    return fade(t,d,.3,.5)*(.34*low+.12*mid)*pulse
colossus_rumble.low,colossus_rumble.mid={"lp1":0.,"lp2":0.},{"lp1":0.,"lp2":0.}
make("colossus_rumble",2.8,colossus_rumble)

def nightmare_voice(name, base, duration, roughness, pulse_rate):
    state = {"lp1": 0.0, "lp2": 0.0}
    grit = {"lp1": 0.0, "lp2": 0.0}
    def sample(t, d, i, n):
        p = t / d
        f = base * (1.0 + 0.18 * math.sin(2 * math.pi * 0.37 * t) + 0.12 * p)
        phase = 2 * math.pi * (base * t + base * 0.06 * t * t / d)
        low = noise(t, max(22, base * 0.42), max(180, base * 3.8), state)
        high = noise(t, max(260, base * 2.2), min(7000, base * 13), grit)
        pulse = 0.25 + 0.75 * max(0.0, math.sin(2 * math.pi * pulse_rate * t + 0.4)) ** 2
        tone = math.sin(phase) + 0.32 * math.sin(phase * 1.51 + 0.7)
        return fade(t, d, 0.08, min(0.5, d * 0.24)) * pulse * (
            0.22 * tone + roughness * 0.32 * low + roughness * 0.18 * high)
    make(name, duration, sample)

nightmare_voice("nightmare_watcher", 310, 2.4, 0.24, 0.62)
nightmare_voice("nightmare_hound", 68, 1.55, 0.62, 1.8)
nightmare_voice("nightmare_wardened", 43, 2.8, 0.38, 0.42)
nightmare_voice("nightmare_false_survivor", 176, 2.1, 0.31, 0.84)
nightmare_voice("nightmare_unbound", 92, 2.35, 0.58, 1.15)

def stalker_joint(t, d, i, n):
    low = noise(t, 55, 420, stalker_joint.low)
    crack = noise(t, 900, 6200, stalker_joint.crack)
    impacts = 0.0
    for at, pitch in ((0.08, 740), (0.31, 1120), (0.68, 510)):
        x = t - at
        if 0.0 <= x < 0.075:
            impacts += 0.36 * math.sin(2 * math.pi * pitch * x) * math.exp(-x * 48)
    return fade(t, d, 0.002, 0.18) * (0.28 * low * math.exp(-t * 1.5)
            + 0.18 * crack * math.exp(-t * 4.0) + impacts)
stalker_joint.low, stalker_joint.crack = {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0}
make("stalker_joint", 0.92, stalker_joint)

def stalker_lunge(t, d, i, n):
    sub = noise(t, 28, 220, stalker_lunge.sub)
    throat = noise(t, 180, 1450, stalker_lunge.throat)
    grit = noise(t, 1300, 7000, stalker_lunge.grit)
    snap = 0.0
    if t < 0.045:
        snap = 0.48 * math.sin(2 * math.pi * (210 - 90 * t) * t) * math.exp(-t * 28)
    sweep = math.sin(2 * math.pi * (95 * t - 32 * t * t))
    return snap + 0.34 * sub * math.exp(-t * 4.0) + 0.22 * throat * math.exp(-t * 3.0) + 0.16 * grit * math.exp(-t * 7.0) + 0.13 * sweep * math.exp(-t * 5.0)
stalker_lunge.sub, stalker_lunge.throat, stalker_lunge.grit = (
    {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0})
make("stalker_lunge", 0.88, stalker_lunge)

def hunter_pounce(t, d, i, n):
    sub = noise(t, 32, 240, hunter_pounce.sub)
    breath = noise(t, 140, 1500, hunter_pounce.breath)
    grit = noise(t, 1000, 6800, hunter_pounce.grit)
    snap = 0.0
    if t < 0.055:
        snap = 0.52 * math.sin(2 * math.pi * (260 - 120 * t) * t) * math.exp(-t * 25)
    rush = math.sin(2 * math.pi * (120 * t - 42 * t * t))
    return snap + 0.30 * sub * math.exp(-t * 3.8) + 0.25 * breath * math.exp(-t * 3.0) + 0.18 * grit * math.exp(-t * 7.0) + 0.16 * rush * math.exp(-t * 4.5)
hunter_pounce.sub, hunter_pounce.breath, hunter_pounce.grit = (
    {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0})
make("hunter_pounce", 0.98, hunter_pounce)

def nightmare_screech(t, d, i, n):
    p = t / d
    f = 190 - 105 * p + 22 * math.sin(2 * math.pi * 7.0 * t)
    phase = 2 * math.pi * (190 * t - 52.5 * t * t / d)
    low = noise(t, 35, 620, nightmare_screech.low)
    rasp = noise(t, 700, 6500, nightmare_screech.rasp)
    pulse = 0.35 + 0.65 * max(0.0, math.sin(2 * math.pi * 5.2 * t)) ** 2
    return fade(t, d, 0.006, 0.24) * pulse * (
        0.25 * math.sin(phase) + 0.16 * math.sin(phase * 1.73 + 0.8)
        + 0.27 * low + 0.20 * rasp)
nightmare_screech.low, nightmare_screech.rasp = {"lp1": 0.0, "lp2": 0.0}, {"lp1": 0.0, "lp2": 0.0}
make("nightmare_screech", 1.15, nightmare_screech)
