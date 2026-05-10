"""Generate gathering_chest.png (176x256) for the Gathering Chest GUI."""
import struct, zlib, os

W, H = 176, 256

# RGBA pixel grid
pixels = [[[198, 198, 198, 255] for _ in range(W)] for _ in range(H)]

def set_px(x, y, r, g, b, a=255):
    if 0 <= x < W and 0 <= y < H:
        pixels[y][x] = [r, g, b, a]

def draw_slot(sx, sy):
    """Draw an 18x18 slot border; sx,sy = top-left of the 16x16 item interior."""
    x, y = sx - 1, sy - 1
    # Top and left: dark shadow
    for i in range(18):
        set_px(x + i, y,      85,  85,  85)  # top row
        set_px(x,     y + i,  85,  85,  85)  # left col
    # Bottom and right: light highlight
    for i in range(18):
        set_px(x + i, y + 17, 255, 255, 255)  # bottom row
        set_px(x + 17, y + i, 255, 255, 255)  # right col
    # Interior
    for row in range(1, 17):
        for col in range(1, 17):
            set_px(x + col, y + row, 139, 139, 139)

def draw_separator_line(y):
    """Draw a subtle 1px horizontal separator line."""
    for x in range(7, 169):
        set_px(x, y, 170, 170, 170)

# ── Storage slots (6 rows x 9 cols) ──────────────────────────────────────────
for row in range(6):
    for col in range(9):
        draw_slot(8 + col * 18, 18 + row * 18)

# Separator above "Void Filter" label
draw_separator_line(126)

# ── Filter slots (1 row x 9 cols) ────────────────────────────────────────────
# Slightly different background tint to visually distinguish filter area
for x in range(5, 172):
    for y_f in range(134, 159):
        pixels[y_f][x] = [188, 188, 198, 255]  # slight blue tint

for col in range(9):
    draw_slot(8 + col * 18, 140)

# Separator above player inventory
draw_separator_line(160)

# ── Player inventory (3 rows x 9 cols) ───────────────────────────────────────
for row in range(3):
    for col in range(9):
        draw_slot(8 + col * 18, 172 + row * 18)

# ── Hotbar (1 row x 9 cols) ──────────────────────────────────────────────────
for col in range(9):
    draw_slot(8 + col * 18, 230)

# ── Write PNG ─────────────────────────────────────────────────────────────────
def write_png(path, px, w, h):
    def pack_chunk(name, data):
        crc = zlib.crc32(name + data) & 0xffffffff
        return struct.pack('>I', len(data)) + name + data + struct.pack('>I', crc)

    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)  # 8-bit RGBA
    rows = bytearray()
    for row in px:
        rows += b'\x00'  # filter type None
        for p in row:
            rows += bytes(p)
    idat = zlib.compress(bytes(rows), 9)

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(pack_chunk(b'IHDR', ihdr))
        f.write(pack_chunk(b'IDAT', idat))
        f.write(pack_chunk(b'IEND', b''))
    print(f"Written: {path} ({w}x{h})")

out = os.path.join(os.path.dirname(__file__),
    'src', 'main', 'resources', 'assets', 'hydroponicraft',
    'textures', 'gui', 'container', 'gathering_chest.png')

write_png(out, pixels, W, H)
