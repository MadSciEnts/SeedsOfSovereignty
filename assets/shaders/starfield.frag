#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;

uniform vec2 u_cameraPos;
uniform vec2 u_resolution;

// Simple hash function for randomness
float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

vec3 drawLayer(vec2 pixelCoord, vec2 scroll, float cellSize, float threshold) {
    vec2 pCoord = pixelCoord + scroll;
    vec2 gridId = floor(pCoord / cellSize);
    vec2 cellCoord = mod(pCoord, cellSize);

    float h = hash(gridId);
    if (h > threshold) {
        // Random offset within the cell
        vec2 offset = vec2(hash(gridId + 0.1), hash(gridId + 0.2)) * cellSize;
        float dist = length(cellCoord - offset);

        // Larger star size: radius 1.5 to 3.0 pixels (3-6px diameter)
        // This ensures stars always occupy multiple pixels, reducing alias flicker.
        float size = mix(1.5, 3.0, hash(gridId + 0.3));

        // Very soft Gaussian-like falloff for maximum stability during movement
        float core = exp(-2.0 * (dist * dist) / (size * size));
        // Add a secondary wide glow to ensure some energy always lands on neighboring pixels
        float glow = exp(-1.0 * dist / (size * 2.0)) * 0.2;

        float brightness = core + glow;

        if (brightness > 0.01) {
            // Hue variation
            vec3 starColor = vec3(1.0);
            float hueH = hash(gridId + 0.4);
            if (hueH < 0.2) starColor = vec3(0.75, 0.85, 1.0); // Technical Blue
            else if (hueH < 0.4) starColor = vec3(1.0, 0.95, 0.7); // Scientific Yellow
            else if (hueH < 0.5) starColor = vec3(1.0, 0.8, 0.8); // Red Dwarf

            // Base luminosity variation
            float bVar = hash(gridId + 0.5) * 0.5 + 0.5;

            return starColor * bVar * brightness;
        }
    }
    return vec3(0.0);
}

void main() {
    // Correct UV aspect ratio by working in pixel space
    vec2 pixelCoord = v_texCoord * u_resolution;

    // Deeper technical background
    vec3 finalColor = vec3(0.002, 0.002, 0.01);

    // Layer 1: Far (slowest, high density, smallest)
    finalColor += drawLayer(pixelCoord, u_cameraPos * 1.2, 40.0, 0.92);

    // Layer 2: Mid
    finalColor += drawLayer(pixelCoord, u_cameraPos * 5.0, 80.0, 0.96);

    // Layer 3: Near (fastest, low density, largest)
    finalColor += drawLayer(pixelCoord, u_cameraPos * 12.0, 160.0, 0.99);

    gl_FragColor = vec4(finalColor, 1.0);
}
