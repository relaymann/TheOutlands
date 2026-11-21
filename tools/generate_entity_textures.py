#!/usr/bin/env python3
"""Generate deterministic 64x64 pixel-art skins for every Outlands creature.
Uses only the Python standard library so local and CI builds produce identical assets.
"""
import math, os, random, struct, zlib

OUT = os.path.join("src", "main", "resources", "assets", "theoutlands", "textures", "entity")
PALETTES = {
 "null": ((5,7,11),(17,20,27),(35,39,48),(80,88,102),(180,190,205)),
 "fear": ((8,9,16),(22,23,36),(43,46,66),(102,109,139),(190,198,225)),
 "death": ((12,10,12),(35,28,29),(72,55,52),(130,104,89),(206,177,144)),
 "almighty": ((8,5,18),(29,14,48),(66,28,92),(126,57,155),(220,160,255)),
 "null_apostle": ((2,3,5),(9,11,15),(23,26,33),(56,62,73),(116,124,137)),
 "cave_jar": ((7,8,9),(23,25,25),(48,53,51),(92,104,97),(161,174,152)),
 "stalker": ((4,6,9),(15,20,25),(33,43,48),(74,89,91),(135,153,147)),
 "observer": ((9,11,20),(24,30,46),(47,61,79),(95,121,140),(183,207,214)),
 "hunter": ((10,9,8),(30,26,22),(61,49,38),(111,88,61),(174,139,95)),
 "burrower": ((13,10,8),(39,28,20),(77,54,35),(127,89,54),(188,142,93)),
 "mimic": ((9,13,10),(25,37,25),(48,67,39),(91,116,57),(165,174,91)),
 "stillborn_statue": ((14,15,17),(39,42,45),(73,78,81),(119,126,128),(187,194,190)),
 "agitated_one": ((13,6,5),(43,15,12),(87,27,20),(147,54,38),(223,117,70)),
 "lurker": ((6,9,8),(17,25,20),(37,51,37),(73,91,58),(133,146,92)),
 "howler": ((11,8,10),(31,20,25),(66,39,45),(120,69,70),(190,126,110)),
 "dredge": ((5,8,11),(15,23,28),(34,48,53),(72,91,94),(135,155,149)),
 "pilgrim": ((9,8,7),(27,24,21),(57,49,41),(105,91,76),(165,151,127)),
 "ceiling_dweller": ((5,7,8),(16,22,24),(38,49,51),(83,99,98),(155,169,158)),
 "colossus": ((8,8,7),(25,25,22),(53,54,47),(96,100,86),(158,164,139)),
}

def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)

def write_png(path, pixels):
    raw = b"".join(b"\x00" + bytes(channel for rgb in row for channel in rgb) for row in pixels)
    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB",64,64,8,2,0,0,0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    with open(path, "wb") as f: f.write(data)

def line(pixels, x0, y0, x1, y1, color):
    dx, sx = abs(x1-x0), 1 if x0<x1 else -1
    dy, sy = -abs(y1-y0), 1 if y0<y1 else -1
    err = dx + dy
    while True:
        if 0 <= x0 < 64 and 0 <= y0 < 64: pixels[y0][x0] = color
        if x0 == x1 and y0 == y1: break
        e2 = 2*err
        if e2 >= dy: err += dy; x0 += sx
        if e2 <= dx: err += dx; y0 += sy

def rect(pixels, x0, y0, x1, y1, color):
    for y in range(max(0,y0), min(64,y1+1)):
        for x in range(max(0,x0), min(64,x1+1)): pixels[y][x] = color

def generate(name, palette, index):
    rng = random.Random(20261120 + index*173)
    pixels = []
    for y in range(64):
        row=[]
        for x in range(64):
            grain = rng.random()*0.65 + 0.18*math.sin((x+index*3)*0.39) + 0.12*math.cos(y*.31+x*.13)
            band = (x//4 + y//8 + index) % 5
            shade = max(0,min(4,int(grain*3.0)+(1 if band==3 and rng.random()<.25 else 0)))
            row.append(palette[shade])
        pixels.append(row)
    for _ in range(24):
        x,y=rng.randrange(2,62),rng.randrange(2,62)
        c=palette[rng.choice((0,1,2,3))]
        if name in ("stillborn_statue","death","cave_jar","burrower","dredge"):
            line(pixels,x,y,x+rng.randrange(-3,4),y+rng.randrange(2,9),c)
        else:
            rect(pixels,x,y,x+rng.randrange(1,3),y+rng.randrange(1,4),c)
    # Standard 64x64 humanoid face UV: front panel is x=8..15, y=8..15.
    if name not in ("burrower",):
        eye = palette[3] if name in ("null","null_apostle","almighty") else palette[4]
        rect(pixels,8,9,9,10,eye); rect(pixels,13,9,14,10,eye)
        if name in ("howler","mimic","agitated_one"):
            rect(pixels,10,13,13,14,palette[0])
        else:
            line(pixels,10,14,13,14,palette[0])
    # Individual signature marks in torso/limb UV space.
    marks = {
      "death":((27,22,24,30),(29,34,25,39)),
      "fear":((25,19,28,24),(26,29,31,35)),
      "almighty":((28,17,32,25),(27,33,34,41)),
      "null_apostle":((28,23,35,32),(25,20,38,36)),
      "cave_jar":((23,19,21,37),(37,19,35,37)),
      "stalker":((26,19,30,25),(26,31,32,38)),
      "observer":((28,22,35,29),(31,25,31,25)),
      "hunter":((25,19,31,25),(31,25,36,21)),
      "burrower":((4,31,7,39),(55,31,58,39)),
      "mimic":((25,20,32,27),(32,27,39,20)),
      "stillborn_statue":((24,20,39,18),(24,30,39,28)),
      "agitated_one":((26,19,31,27),(31,27,26,34)),
      "lurker":((26,20,31,27),(28,33,35,38)),
      "pilgrim":((25,20,31,29),(28,34,35,41)), "ceiling_dweller":((22,24,26,18),(38,24,40,18)), "colossus":((25,24,31,34),(31,34,37,24)),
      "howler":((29,19,32,28),(32,28,29,35)),
      "dredge":((23,25,25,18),(35,25,37,18)),
    }
    for coords in marks.get(name, ()):
        x0,y0,x1,y1=coords
        line(pixels,x0,y0,x1,y1,palette[4] if name not in ("null","null_apostle") else palette[3])
    if name == "observer":
        # The single unblinking eye mark.
        rect(pixels,29,24,34,25,palette[4])
    if name == "cave_jar":
        for x in (25,30,35): line(pixels,x,20,x-2,37,palette[3])
    if name == "lurker":
        for x in (25,31,37): line(pixels,x,18,x+2,13,palette[4])
    if name == "dredge":
        for x in (23,29,35): line(pixels,x,25,x+2,18,palette[4])
    os.makedirs(OUT, exist_ok=True)
    write_png(os.path.join(OUT,name+".png"), pixels)

def main():
    for index,(name,palette) in enumerate(PALETTES.items()):
        generate(name,palette,index)
    print("Generated",len(PALETTES),"unique 64x64 Outlands entity textures.")

if __name__ == "__main__": main()
