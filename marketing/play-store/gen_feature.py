from PIL import Image, ImageDraw, ImageFont

W, H = 1024, 500

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i]-a[i])*t) for i in range(3))

def hex2rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2],16) for i in (0,2,4))

start = hex2rgb('FF2E78')
center = hex2rgb('E91E63')
end = hex2rgb('9C27B0')

bg = Image.new('RGB', (W, H))
px = bg.load()
for y in range(H):
    for x in range(W):
        t = (x + y) / (W + H)
        t = max(0.0, min(1.0, t))
        if t < 0.5:
            c = lerp(start, center, t / 0.5)
        else:
            c = lerp(center, end, (t - 0.5) / 0.5)
        px[x, y] = c

heart = Image.open('app/src/main/res/drawable-xxxhdpi/ic_brand_heart_white.png').convert('RGBA')
heart_size = 220
heart_resized = heart.resize((heart_size, heart_size), Image.LANCZOS)
bg.paste(heart_resized, (90, (H - heart_size)//2), heart_resized)

draw = ImageDraw.Draw(bg)
try:
    font_big = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 88)
    font_small = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 34)
except Exception:
    font_big = ImageFont.load_default()
    font_small = ImageFont.load_default()

text_x = 380
draw.text((text_x, 165), "FypMatch", font=font_big, fill=(255,255,255))
draw.text((text_x, 270), "Conex\u00f5es de verdade, feitas pra durar", font=font_small, fill=(255,255,255))

bg.save('marketing/play-store/feature_graphic_1024x500.png', 'PNG')
print('feature graphic saved', bg.size)
