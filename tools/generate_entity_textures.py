#!/usr/bin/env python3
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
 "nightmare_watcher": ((2,3,6),(10,15,23),(28,39,49),(92,121,129),(205,225,218)),
 "nightmare_hound": ((5,3,3),(23,13,12),(61,31,27),(133,66,48),(224,156,112)),
 "nightmare_wardened": ((4,5,7),(18,22,28),(48,56,63),(103,117,121),(184,194,177)),
 "nightmare_false_survivor": ((8,8,7),(27,26,24),(64,59,50),(126,111,89),(202,184,151)),
 "nightmare_unbound": ((3,2,9),(16,9,29),(46,24,69),(111,55,137),(222,142,255)),
}

def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)

def write_png(path, pixels):
    height, width = len(pixels), len(pixels[0])
    raw = b"".join(b"\x00" + bytes(channel for rgb in row for channel in rgb) for row in pixels)
    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB",width,height,8,2,0,0,0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    with open(path, "wb") as f: f.write(data)

def line(pixels, x0, y0, x1, y1, color):
    dx, sx = abs(x1-x0), 1 if x0<x1 else -1
    dy, sy = -abs(y1-y0), 1 if y0<y1 else -1
    err = dx + dy
    while True:
        if 0 <= x0 < len(pixels[0]) and 0 <= y0 < len(pixels): pixels[y0][x0] = color
        if x0 == x1 and y0 == y1: break
        e2 = 2*err
        if e2 >= dy: err += dy; x0 += sx
        if e2 <= dx: err += dx; y0 += sy

def rect(pixels, x0, y0, x1, y1, color):
    for y in range(max(0,y0), min(len(pixels),y1+1)):
        for x in range(max(0,x0), min(len(pixels[0]),x1+1)): pixels[y][x] = color

def generate(name, palette, index):
    rng = random.Random(20261120 + index*173)
    width, height = (128, 128) if name == "colossus" else (64, 128) if name == "pilgrim" else (64, 64)
    pixels = []
    for y in range(height):
        row=[]
        for x in range(width):
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

    if name not in ("burrower",):
        eye = palette[3] if name in ("null","null_apostle","almighty") else palette[4]
        rect(pixels,8,9,9,10,eye); rect(pixels,13,9,14,10,eye)
        if name in ("howler","mimic","agitated_one"):
            rect(pixels,10,13,13,14,palette[0])
        else:
            line(pixels,10,14,13,14,palette[0])

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

    if name in ("stalker", "nightmare_watcher", "nightmare_hound",
                "nightmare_wardened", "nightmare_false_survivor", "nightmare_unbound"):
        eye, shadow, mid = palette[4], palette[0], palette[2]
        rect(pixels, 8, 9, 15, 11, shadow)
        if name in ("stalker", "nightmare_watcher"):
            rect(pixels, 9, 10, 9, 11, eye); rect(pixels, 14, 10, 14, 11, eye)
        elif name == "nightmare_hound":
            rect(pixels, 8, 10, 10, 11, eye); rect(pixels, 13, 10, 15, 11, eye)
        elif name == "nightmare_wardened":
            rect(pixels, 9, 10, 10, 10, eye); rect(pixels, 13, 10, 14, 10, eye)
        elif name == "nightmare_false_survivor":
            rect(pixels, 9, 10, 9, 10, eye); rect(pixels, 14, 10, 14, 11, eye)
        else:
            rect(pixels, 9, 9, 10, 11, eye); rect(pixels, 13, 9, 14, 11, eye)
        if name in ("stalker", "nightmare_hound", "nightmare_unbound"):
            rect(pixels, 10, 13, 13, 15, shadow)
            for tooth_x in (10, 12, 13):
                pixels[13][tooth_x] = palette[4]
                pixels[15][tooth_x] = palette[3]
        elif name == "nightmare_watcher":
            line(pixels, 11, 12, 12, 15, palette[3]); rect(pixels, 10, 14, 13, 14, shadow)
        elif name == "nightmare_wardened":
            line(pixels, 11, 13, 12, 14, palette[3])
        else:
            line(pixels, 11, 12, 12, 15, shadow); pixels[14][11] = palette[4]
        for x in (20, 22, 25, 27):
            line(pixels, x, 21, x + (1 if x < 24 else -1), 29, mid)
        if name in ("stalker", "nightmare_hound", "nightmare_unbound"):
            line(pixels, 23, 20, 24, 31, palette[3])
        if name == "nightmare_wardened":
            rect(pixels, 20, 20, 27, 22, palette[1]); line(pixels, 23, 22, 23, 31, palette[4])
        if name == "nightmare_false_survivor":
            line(pixels, 20, 20, 27, 31, palette[3]); line(pixels, 27, 20, 20, 31, palette[1])

    if name == "observer":

        rect(pixels,29,24,34,25,palette[4])
    if name == "cave_jar":
        for x in (25,30,35): line(pixels,x,20,x-2,37,palette[3])
    if name == "lurker":
        for x in (25,31,37): line(pixels,x,18,x+2,13,palette[4])
    if name == "dredge":
        for x in (23,29,35): line(pixels,x,25,x+2,18,palette[4])
    if name == "pilgrim":
        line(pixels, 27, 18, 27, 43, palette[1])
        line(pixels, 36, 18, 36, 43, palette[1])
        rect(pixels, 29, 31, 34, 34, palette[3])
        line(pixels, 31, 29, 31, 37, palette[4])
        line(pixels, 28, 33, 35, 33, palette[4])
    if name == "ceiling_dweller":
        for x in (8, 13, 18): rect(pixels, x, 9, x+1, 11, palette[4])
        for x in (21, 27, 37, 43): line(pixels, x, 31, x-3, 39, palette[4])
    if name == "colossus":
        for y in range(8, 120, 12): line(pixels, 18, y, 108, y-3, palette[1])
        for x in (20, 42, 66, 90, 112): line(pixels, x, 8, x-5, 120, palette[2])
        for x, y in ((32, 28), (78, 46), (54, 83), (96, 101)):
            line(pixels, x, y, x+7, y+11, palette[4])
            line(pixels, x+7, y+11, x+13, y+15, palette[3])

    if name not in ("burrower", "dredge", "colossus"):

        rect(pixels, 8, 8, 15, 11, palette[0])
        left_eye = palette[4] if name not in ("fear", "null", "null_apostle", "observer") else palette[3]
        right_eye = palette[3] if name in ("death", "hunter", "lurker", "howler", "mimic") else palette[4]
        rect(pixels, 9, 9, 10, 10, left_eye)
        rect(pixels, 13, 9, 14, 11, right_eye)

        if name in ("mimic", "howler", "agitated_one", "cave_jar", "stalker",
                    "death", "hunter", "lurker", "nightmare_hound", "nightmare_unbound"):
            line(pixels, 9, 13, 14, 14, palette[0])
            for tx, ty in ((10,13),(12,14),(14,13)):
                rect(pixels, tx, ty, tx, ty+1, palette[4])
        elif name in ("fear", "null", "null_apostle", "observer", "nightmare_watcher"):
            line(pixels, 10, 14, 13, 13, palette[1])

        if name in ("fear", "death", "null", "null_apostle", "observer", "pilgrim",
                    "almighty", "nightmare_false_survivor", "nightmare_watcher"):
            line(pixels, 21, 20, 23, 30, palette[3])
            line(pixels, 26, 22, 24, 31, palette[1])
            line(pixels, 20, 25, 27, 27, palette[2])

        if name in ("stalker", "hunter", "agitated_one", "cave_jar", "lurker",
                    "howler", "nightmare_hound", "nightmare_unbound"):
            for ry in (22, 25, 28, 31):
                line(pixels, 20, ry, 27, ry + (1 if ry % 2 else 0), palette[3])
            line(pixels, 23, 21, 23, 32, palette[4])

        if name in ("stillborn_statue", "burrower", "dredge", "ceiling_dweller", "colossus"):
            for sx, sy, ex, ey in ((20,20,24,26),(27,22,23,29),(21,31,26,35),(29,30,25,36)):
                line(pixels, sx, sy, ex, ey, palette[3])

        signatures = {
            "null": lambda: (line(pixels, 11, 7, 12, 16, palette[3]), rect(pixels, 8, 23, 9, 28, palette[1])),
            "null_apostle": lambda: (line(pixels, 11, 6, 11, 16, palette[3]), rect(pixels, 22, 20, 26, 21, palette[1])),
            "fear": lambda: (rect(pixels, 10, 8, 13, 11, palette[0]), line(pixels, 11, 12, 12, 15, palette[4])),
            "death": lambda: (line(pixels, 8, 12, 14, 8, palette[4]), line(pixels, 24, 20, 28, 32, palette[4])),
            "almighty": lambda: (rect(pixels, 9, 9, 14, 10, palette[3]), line(pixels, 23, 20, 23, 34, palette[4])),
            "observer": lambda: (rect(pixels, 8, 9, 15, 11, palette[0]), rect(pixels, 10, 9, 13, 10, palette[4])),
            "mimic": lambda: (line(pixels, 8, 8, 14, 15, palette[4]), line(pixels, 14, 8, 9, 15, palette[2])),
            "stillborn_statue": lambda: (line(pixels, 11, 7, 9, 16, palette[4]), line(pixels, 24, 20, 22, 34, palette[1])),
            "pilgrim": lambda: (rect(pixels, 9, 9, 10, 10, palette[4]), rect(pixels, 13, 9, 14, 10, palette[0]), line(pixels, 24, 19, 27, 35, palette[3])),
            "ceiling_dweller": lambda: (rect(pixels, 7, 9, 9, 11, palette[4]), rect(pixels, 12, 9, 14, 11, palette[4]), line(pixels, 22, 22, 18, 35, palette[3])),
        }
        if name in signatures:
            signatures[name]()

    if name == "stalker":
        rect(pixels, 8, 8, 15, 9, palette[0])
        rect(pixels, 9, 10, 9, 11, palette[4])
        rect(pixels, 14, 10, 14, 11, palette[3])
        line(pixels, 10, 12, 13, 15, palette[0])
        line(pixels, 13, 12, 10, 15, palette[0])
        for x,y in ((10,13),(12,14),(14,13)): rect(pixels,x,y,x,y,palette[4])
    elif name == "hunter":
        line(pixels, 8, 8, 15, 9, palette[0])
        rect(pixels, 9, 10, 10, 11, palette[4])
        rect(pixels, 13, 10, 14, 11, palette[4])
        rect(pixels, 10, 12, 13, 15, palette[0])
        for x in (10,12,13): rect(pixels,x,12,x,13,palette[4])
    elif name == "lurker":
        rect(pixels, 8, 9, 15, 10, palette[0])
        rect(pixels, 9, 10, 10, 10, palette[4])
        rect(pixels, 13, 10, 14, 10, palette[4])
        line(pixels, 11, 11, 12, 16, palette[0])
        for y in (13,15): rect(pixels,10,y,13,y,palette[4])
    elif name == "observer":
        rect(pixels, 8, 8, 15, 12, palette[0])
        rect(pixels, 9, 9, 10, 11, palette[3])
        rect(pixels, 12, 9, 14, 11, palette[4])
        rect(pixels, 11, 10, 12, 11, palette[0])
        rect(pixels, 10, 13, 13, 15, palette[0])
        pixels[13][11] = palette[4]
        pixels[14][12] = palette[3]
    elif name == "stillborn_statue":
        rect(pixels, 8, 8, 15, 11, palette[1])
        line(pixels, 9, 9, 10, 11, palette[4])
        line(pixels, 14, 9, 13, 11, palette[4])
        line(pixels, 11, 12, 12, 15, palette[0])
        line(pixels, 8, 7, 11, 13, palette[3])
        line(pixels, 15, 8, 12, 14, palette[3])
    elif name == "nightmare_false_survivor":
        rect(pixels, 8, 8, 15, 9, palette[1])
        rect(pixels, 9, 10, 10, 10, palette[4])
        rect(pixels, 13, 10, 14, 11, palette[0])
        line(pixels, 9, 13, 15, 14, palette[0])
        for x in (10,12,14): rect(pixels,x,13,x,14,palette[4])
        line(pixels, 8, 12, 9, 15, palette[3])
    elif name == "pilgrim":
        rect(pixels, 7, 7, 16, 11, palette[0])
        rect(pixels, 9, 10, 10, 10, palette[4])
        rect(pixels, 13, 10, 14, 10, palette[4])
        line(pixels, 11, 11, 12, 15, palette[1])
        for y in (12,14): rect(pixels,10,y,13,y,palette[3])
    elif name == "agitated_one":
        rect(pixels, 8, 8, 15, 9, palette[0])
        rect(pixels, 9, 9, 10, 11, palette[4])
        rect(pixels, 13, 9, 14, 11, palette[4])
        rect(pixels, 9, 13, 14, 15, palette[0])
        for x in (9,11,13,14): rect(pixels,x,13,x,14,palette[4])
        line(pixels, 11, 11, 12, 16, palette[0])

    if name == "burrower":
        rect(pixels, 7, 8, 16, 10, palette[1])
        rect(pixels, 9, 10, 10, 11, palette[4]); rect(pixels, 13, 10, 14, 11, palette[4])
        line(pixels, 8, 12, 15, 15, palette[0])
        for x in (9,11,13,15): line(pixels,x,12,x+1,15,palette[4])
        for y in (20,23,26,29): line(pixels,20,y,28,y+2,palette[3])
    elif name == "dredge":
        rect(pixels, 7, 8, 16, 11, palette[0])
        rect(pixels, 9, 9, 10, 10, palette[3]); rect(pixels, 13, 9, 14, 10, palette[4])
        line(pixels, 8, 13, 15, 14, palette[0])
        for x in (9,11,13,15): line(pixels,x,12,x-1,16,palette[4])
        for y in (19,22,25,28): line(pixels,20,y,27,y+3,palette[2])
    elif name == "howler":
        rect(pixels, 8, 8, 15, 10, palette[0])
        rect(pixels, 9, 10, 10, 11, palette[4]); rect(pixels, 13, 10, 14, 11, palette[4])
        rect(pixels, 9, 12, 14, 16, palette[0])
        for x in (9,11,13,14): line(pixels,x,12,x,15,palette[4])
        rect(pixels, 10, 22, 13, 31, palette[2])
    elif name == "nightmare_hound":
        rect(pixels, 7, 8, 16, 11, palette[0])
        rect(pixels, 8, 10, 10, 11, palette[4]); rect(pixels, 13, 10, 15, 11, palette[4])
        rect(pixels, 9, 12, 14, 16, palette[0])
        for x in (9,11,13,14): line(pixels,x,12,x,16,palette[4])
        line(pixels, 20, 18, 27, 30, palette[3]); line(pixels,27,18,20,30,palette[3])
    elif name == "cave_jar":
        rect(pixels, 7, 7, 16, 10, palette[1])
        line(pixels, 8, 8, 15, 8, palette[4])
        rect(pixels, 9, 11, 10, 12, palette[4]); rect(pixels, 13, 11, 14, 12, palette[4])
        line(pixels, 10, 13, 13, 15, palette[0])
        for x in (21,25,29,33): line(pixels,x,18,x+2,34,palette[3])
    elif name == "ceiling_dweller":
        rect(pixels, 7, 8, 16, 11, palette[0])
        for x in (8,11,14): rect(pixels,x,9,x+1,10,palette[4])
        line(pixels, 9, 13, 14, 15, palette[0])
        for x in (9,11,13,15): line(pixels,x,13,x,16,palette[4])
        for x in (20,25,30,35): line(pixels,x,20,x-3,31,palette[3])
    elif name == "mimic":
        rect(pixels, 8, 8, 15, 10, palette[0])
        line(pixels, 9, 9, 14, 11, palette[4])
        line(pixels, 14, 9, 9, 11, palette[3])
        line(pixels, 8, 13, 15, 15, palette[0])
        for x in (9,11,13,15): rect(pixels,x,13,x,15,palette[4])
        line(pixels, 20, 20, 27, 31, palette[3]); line(pixels,27,20,20,31,palette[2])
    elif name == "death":
        rect(pixels, 7, 7, 16, 10, palette[0])
        rect(pixels, 9, 10, 10, 11, palette[3]); rect(pixels, 13, 10, 14, 11, palette[3])
        line(pixels, 11, 11, 12, 15, palette[0])
        for x in (9,11,13,15): rect(pixels,x,13,x,14,palette[4])
        line(pixels, 20, 19, 27, 32, palette[3]); line(pixels,27,19,20,32,palette[3])

    if name == "almighty":
        rect(pixels, 7, 7, 16, 12, palette[0])
        line(pixels, 8, 8, 15, 8, palette[3])
        rect(pixels, 10, 9, 13, 10, palette[4])
        line(pixels, 11, 10, 12, 14, palette[0])
        for x in (9,12,15): line(pixels,x,12,x+1,16,palette[3])
        for x in (20,25,30,35): line(pixels,x,18,x+3,31,palette[3])
    elif name == "colossus":
        rect(pixels, 6, 7, 17, 12, palette[1])
        rect(pixels, 8, 9, 10, 10, palette[0]); rect(pixels, 14, 9, 16, 10, palette[0])
        line(pixels, 8, 13, 15, 13, palette[0])
        for x in (9,11,13,15): line(pixels,x,13,x,17,palette[4])
        for y in (20,24,28,32): line(pixels,20,y,35,y+2,palette[3])
    elif name == "fear":
        rect(pixels, 7, 7, 16, 12, palette[0])
        line(pixels, 8, 8, 11, 11, palette[3]); line(pixels,15,8,12,11,palette[3])
        rect(pixels, 10, 9, 13, 11, palette[0])
        rect(pixels, 9, 13, 14, 16, palette[3])
        for x in (9,12,15): line(pixels,x,18,x-2,32,palette[4])
    elif name == "null":
        rect(pixels, 7, 7, 16, 12, palette[0])
        rect(pixels, 9, 9, 10, 11, palette[3]); rect(pixels, 13, 9, 14, 11, palette[3])
        rect(pixels, 11, 10, 12, 14, palette[0])
        rect(pixels, 8, 14, 15, 16, palette[0])
        for x in (9,12,15): line(pixels,x,18,x+2,33,palette[3])
    elif name == "null_apostle":
        rect(pixels, 7, 7, 16, 12, palette[1])
        line(pixels, 8, 8, 15, 8, palette[3])
        rect(pixels, 9, 10, 10, 11, palette[4]); rect(pixels, 13, 10, 14, 11, palette[4])
        line(pixels, 11, 12, 12, 15, palette[0])
        for x in (9,12,15): line(pixels,x,18,x,33,palette[3])
    elif name == "nightmare_unbound":
        rect(pixels, 6, 7, 17, 12, palette[0])
        line(pixels, 7, 8, 10, 5, palette[3]); line(pixels,16,8,13,5,palette[3])
        rect(pixels, 8, 9, 10, 11, palette[4]); rect(pixels, 13, 9, 15, 11, palette[3])
        rect(pixels, 9, 13, 14, 16, palette[0])
        for x in (9,11,13,15): line(pixels,x,13,x+1,17,palette[4])
        line(pixels, 20, 18, 27, 33, palette[3]); line(pixels,35,18,28,33,palette[2])
    elif name == "nightmare_wardened":
        rect(pixels, 6, 7, 17, 12, palette[1])
        line(pixels, 7, 7, 10, 3, palette[3]); line(pixels,16,7,13,3,palette[3])
        rect(pixels, 8, 9, 10, 10, palette[4]); rect(pixels, 13, 9, 15, 10, palette[4])
        line(pixels, 9, 13, 14, 13, palette[0])
        for x in (8,12,16): line(pixels,x,18,x,34,palette[3])
    elif name == "nightmare_watcher":
        rect(pixels, 6, 7, 17, 12, palette[0])
        for x in (8,11,14,16): rect(pixels,x,8,x+1,10,palette[4])
        rect(pixels, 9, 12, 14, 14, palette[3])
        line(pixels, 11, 13, 12, 16, palette[0])
        for x in (20,25,30,35): line(pixels,x,18,x-2,33,palette[3])

    sclera = palette[4] if name not in ("fear", "null", "null_apostle", "observer") else palette[3]
    iris = palette[2] if name not in ("observer", "fear") else palette[1]
    pupil = palette[0]
    rect(pixels, 48, 56, 51, 59, sclera)
    rect(pixels, 49, 56, 50, 59, iris)
    rect(pixels, 49, 57, 50, 58, pupil)
    pixels[48][48] = palette[4]
    pixels[48][51] = palette[4]
    rect(pixels, 56, 56, 59, 59, pupil)
    rect(pixels, 57, 56, 58, 57, iris)
    pixels[57][57] = palette[4]

    for lx in (1, 17, 40, 56):
        for ly in (24, 28, 32, 36):
            if lx < width and ly < height and (lx + ly + index) % 3 == 0:
                pixels[ly][lx] = palette[3]

    target_dir = os.path.join(OUT, "nightmare") if name.startswith("nightmare_") else OUT
    os.makedirs(target_dir, exist_ok=True)
    filename = name.removeprefix("nightmare_") if name.startswith("nightmare_") else name
    write_png(os.path.join(target_dir, filename+".png"), pixels)

def main():
    for index,(name,palette) in enumerate(PALETTES.items()):
        generate(name,palette,index)
    print("Generated Outlands textures: standard 64x64, Pilgrim 64x128, Colossus 128x128.")

if __name__ == "__main__": main()
