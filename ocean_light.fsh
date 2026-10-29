#version 150

uniform sampler2D DepthSampler;
uniform mat4 InvProj;
uniform mat4 Proj;
// Cada matriz guarda 4 luces (una por columna).
// Pos*: xyz = posicion en espacio de camara, w = radio (0 = luz vacia)
// Col*: rgb = color, a = intensidad
uniform mat4 PosA;
uniform mat4 PosB;
uniform mat4 ColA;
uniform mat4 ColB;
// x = fuerza de las sombras (0 = la luz atraviesa todo, 1 = las paredes la bloquean del todo)
uniform vec4 Params;

in vec2 texCoord;
out vec4 fragColor;

vec3 viewPos(vec2 uv, float d) {
    vec4 ndc = vec4(uv * 2.0 - 1.0, d * 2.0 - 1.0, 1.0);
    vec4 v = InvProj * ndc;
    return v.xyz / v.w;
}

vec3 lightContribution(vec3 p, vec3 n, vec4 lp, vec4 lc) {
    if (lp.w <= 0.0) {
        return vec3(0.0);
    }
    vec3 toL = lp.xyz - p;
    float dist = length(toL);
    if (dist >= lp.w || dist < 0.001) {
        return vec3(0.0);
    }
    vec3 L = toL / dist;
    float ndl = max(dot(n, L), 0.0);

    // Caida suave: solo un reflejo de luz cerca de la fuente
    float atten = pow(1.0 - dist / lp.w, 2.0);
    float lum = atten * (0.15 + 0.85 * ndl);
    if (lum <= 0.002) {
        return vec3(0.0);
    }

    // Una pared (o bloque) entre la luz y la superficie tapa la luz.
    // Nunca se bloquea del todo: queda un resto (1 - fuerza) para que siga viendose.
    float shadow = 1.0;
    vec3 q = p + n * 0.08;
    float maxT = min(dist, 16.0);
    for (int i = 1; i <= 14; i++) {
        vec3 s = q + L * (maxT * float(i) / 15.0);
        vec4 clip = Proj * vec4(s, 1.0);
        if (clip.w <= 0.0) {
            break;
        }
        vec2 suv = (clip.xy / clip.w) * 0.5 + 0.5;
        if (suv.x < 0.0 || suv.x > 1.0 || suv.y < 0.0 || suv.y > 1.0) {
            break;
        }
        float sd = texture(DepthSampler, suv).r;
        vec3 sp = viewPos(suv, sd);
        float occ = sp.z - s.z;
        if (occ > 0.2 && occ < 6.0) {
            shadow = 1.0 - Params.x;
            break;
        }
    }

    return lc.rgb * lc.a * lum * shadow;
}

void main() {
    float d = texture(DepthSampler, texCoord).r;
    if (d >= 0.99999) {
        discard;
    }

    vec3 p = viewPos(texCoord, d);
    vec3 n = normalize(cross(dFdx(p), dFdy(p)));
    if (dot(n, p) > 0.0) {
        n = -n;
    }

    vec3 total = vec3(0.0);
    for (int i = 0; i < 4; i++) {
        total += lightContribution(p, n, PosA[i], ColA[i]);
        total += lightContribution(p, n, PosB[i], ColB[i]);
    }

    // Factor general bajo: el efecto es un reflejo sutil, no una luz fuerte
    total *= 0.45;

    if (total.r + total.g + total.b <= 0.002) {
        discard;
    }
    fragColor = vec4(total, 1.0);
}
