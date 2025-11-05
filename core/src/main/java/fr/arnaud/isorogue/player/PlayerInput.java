package fr.arnaud.isorogue.player;

public class PlayerInput {

    private float forward;
    private float sideways;
    private boolean jump;
    private boolean sneak;

    public void update(float forward, float sideways, boolean jump, boolean sneak) {
        this.forward = forward;
        this.sideways = sideways;
        this.jump = jump;
        this.sneak = sneak;
    }

    public float getForward() { return forward; }
    public float getSideways() { return sideways; }
    public boolean isJumping() { return jump; }
    public boolean isSneaking() { return sneak; }
}