#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;

void main() {
    // Truth on Disk: High-Energy Burn Shader
    // We use pre-multiplied alpha logic for pure additive stacking (GL_ONE, GL_ONE)
    vec3 rgb = v_color.rgb * v_color.a;

    // Calculate luminance to identify "hot" spots
    float luma = dot(rgb, vec3(0.3, 0.59, 0.11));

    // Overdrive highlights: areas where energy overlaps will bloom into white
    // This removes the "grey/dim" clamping and ensures pure white centers.
    vec3 bloom = rgb + (vec3(1.0, 1.2, 1.5) * pow(luma, 2.0) * 3.0);

    gl_FragColor = vec4(bloom, v_color.a);
}
