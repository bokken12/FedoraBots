"""
This script generates an svg file named robot.svg in the current directory. This
image contains the robot base, thrusters, and thruster exhaust.

This image is copied and pasted into tank.svg, which contains additional parts
such as the blaster and some texture.
"""

import math

radius = 10*3.2 # Radius of the bot
pad = 4 # Angle between engine and body
ir = 8*3.2 # Inner radius
out = 12*3.2 # Radius of outer engines

da = 65 # Angle between two back engines
rots = [90, 270-da/2, 270+da/2] # Radial positions of engies
angles = [32, 42, 42] # Angular widths of engines

thrustpad = 4 # Angle between thrust and
engineh = 8*3.2 # Height of engines

DECIMALS = 10 # Number of decimal places in numbers

WIDTH, HEIGHT = 2*radius, 2*radius

def form(*array):
    def formatEl(el):
        if isinstance(el, float):
            return str(round(float(el), DECIMALS))
        else:
            return str(el)

    return ' '.join(formatEl(x) for x in array)

def move(x, y, absolute=False):
    return form('M' if absolute else 'm', x, y)

def line(x, y, absolute=False):
    return form('L' if absolute else 'l', x, y)

def arc(rx, ry, endx, endy, xrot=0, large=False, sweep=False, absolute=False):
    return form('A' if absolute else 'a', rx, ry, xrot, 1 if large else 0, 1 if sweep else 0, endx, endy)

def carc(r, start_theta, end_theta, large=False, sweep=False):
    x, y = relative(polar(r, start_theta), polar(r, end_theta))
    return arc(r, r, x, y, 0, large, sweep, False)

def genpaths(array):
    for d, fill, opacity, pid in array:
        yield '<path d="{}" fill="{}"  fill-opacity="{}" id="{}"/>'.format(' '.join(d), fill, opacity, pid)

def svg(paths):
    return """<?xml version="1.0" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg width="{0}" height="{1}" viewBox="0 0 {0} {1}" xmlns="http://www.w3.org/2000/svg" version="1.1">
<title>OpenSCAD Model</title>
<defs>
{2}
</defs>
{3}
</svg>""".format(WIDTH, HEIGHT, gradients(), '\n'.join(genpaths(paths)))

def polar(r, theta):
    return r * math.cos(math.radians(theta)), -r * math.sin(math.radians(theta))

def relative(p1, p2):
    return p2[0] - p1[0], p2[1] - p1[1]

##############################################################################################
#                                    Start of definitionv                                    #
##############################################################################################

def linearbounds(theta, rwidth):
    p1 = relative((-WIDTH/2, -HEIGHT/2), polar(out*math.cos(math.radians(rwidth/2)), theta))
    p2 = relative(relative(polar(engineh/1.2, theta), (0,0)), p1)
    return tuple(round(x, DECIMALS) for x in p1), tuple(round(x, DECIMALS) for x in p2)

def radialcenter(theta, rwidth):
    p = relative((-WIDTH/2, -HEIGHT/2), polar(out*math.cos(math.radians(rwidth/2)), theta))
    return tuple(round(x, DECIMALS) for x in p)

def gradients():
    return ''.join(
    """
    <linearGradient id="grad{0}" x1="{2[0]}" y1="{2[1]}" x2="{3[0]}" y2="{3[1]}" gradientUnits="userSpaceOnUse">
    <stop offset="0%" stop-color="lightskyblue"/>
    <stop offset="100%" stop-color="lightskyblue" stop-opacity="0" />
    </linearGradient>
    <radialGradient id="gradr{0}" cx="{4}" cy="{5}" r="{1}" gradientUnits="userSpaceOnUse">
    <stop offset="0%" stop-color="lightskyblue"/>
    <stop offset="100%" stop-color="lightskyblue" stop-opacity="0" />
    </radialGradient>
    """.format(i, round(engineh/1.5, DECIMALS), *(linearbounds(x, y) + radialcenter(x, y)))
        for i, (x, y) in enumerate(zip(rots, angles)))

def sectorpoly(r, start_theta, end_theta, startOrigin=False):
    p1 = polar(r, start_theta)
    p2 = polar(r, end_theta)
    if startOrigin:
        return [line(*relative(p1, p2))]
    else:
        return [line(*p1), line(*relative(p1, p2)), line(*relative(p2, (0, 0)))]

# The engines
engines = [move(WIDTH/2, HEIGHT/2, True)]
for rot, angle in zip(rots, angles):
    engines.extend(sectorpoly(out, rot-angle/2, rot+angle/2, False))

# The body
lastrot = rots[-1] + angles[-1]/2 + pad
body = [move(*relative((-WIDTH/2, -HEIGHT/2), polar(radius, lastrot)), absolute=True)]
for rot, angle in zip(rots, angles):
    fromrot = lastrot
    torot = rot - angle/2 - pad
    body.append(carc(radius, fromrot, torot, torot - fromrot > 180, False))
    body.append(line(*relative(polar(radius, torot), polar(ir, torot))))
    body.extend(sectorpoly(ir, rot-angle/2-pad, rot+angle/2+pad, True))

    lastrot = rot + angle/2 + pad
    body.append(line(*relative(polar(ir, lastrot), polar(radius, lastrot))))

fuel = []
for i, (rot, angle) in enumerate(zip(rots, angles)):
    base_angle = (180 - angle) / 2
    thrust_engine_angle = 180 - base_angle - thrustpad
    # Law of sines
    r = out / math.sin(math.radians(thrust_engine_angle)) * math.sin(math.radians(base_angle))
    p1 = relative((-WIDTH/2, -HEIGHT/2), polar(r, rot + angle/2 - thrustpad))
    p2 = relative((-WIDTH/2, -HEIGHT/2), polar(r, rot - angle/2 + thrustpad))
    path = [
        move(*p1, absolute=True),
        line(*p2, absolute=True),
        line(*relative(relative(p2, (0, 0)), polar(engineh, rot)), absolute=True),
        line(*relative(relative(p1, (0, 0)), polar(engineh, rot)), absolute=True)
    ]
    fuel.append((path, 'url(#grad{})'.format(i), 1, 'thruster{}'.format(i)))
    fuel.append((path, 'url(#gradr{})'.format(i), 1, 'thruster{}-radial'.format(i)))

paths = fuel + [
    (engines, '#333', 0.7, None),
    (body, '#CCC', 1, 'body'),
]

with open('robot.svg', 'w') as f:
    f.write(svg(paths))
