attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord;

void main() {
    v_texCoord = a_texCoord0;
    // Bypass projection matrix to ensure the quad fills the screen (-1 to 1)
    gl_Position = vec4(a_position.xy, 0.0, 1.0);
}
