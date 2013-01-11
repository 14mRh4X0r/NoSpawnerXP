import java.util.logging.Logger;

public class NoSpawnerXP extends Plugin {
    private static final Logger LOG = Logger.getLogger("Minecraft.NoSpawnerXP");

    static {
        LOG.info("[NoSpawnerXP] Injecting patch into proper classloader...");
        // Some magic to make sure SpawnerPatch can call super.
        // This assumes the class loader is an URLClassLoader.
        try {
            ClassLoader cl = OBlockMobSpawner.class.getClassLoader();
            java.lang.reflect.Method m = java.net.URLClassLoader.class
                .getDeclaredMethod("addURL", java.net.URL.class);
            m.setAccessible(true);
            m.invoke(cl, new java.io.File("plugins/NoSpawnerXP.jar").toURI()
                    .toURL());
            cl.loadClass("NoSpawnerXP$SpawnerPatch");
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE,
                    "[NoSpawnerXP] Exception while injecting patch:", e);
        }
    }

    @Override
    public void enable() {
        if (OBlock.p[52] instanceof SpawnerPatch) {
            LOG.info("[NoSpawnerXP] Patch already applied.");
        } else if (SpawnerPatch.class.getClassLoader() !=
                OBlockMobSpawner.class.getClassLoader()) {
            LOG.info("[NoSpawnerXP] Not applying patch, injection failed.");
            etc.getServer().addToServerQueue(new Runnable() {
                @Override
                public void run() {
                    etc.getLoader().disablePlugin("NoSpawnerXP");
                }
            });
        } else {
            LOG.info("[NoSpawnerXP] Applying patch...");
            OBlock.p[52] = null; // Unset current mob spawner block
            new SpawnerPatch(); // Actual patch
        }
        LOG.info("NoSpawnerXP enabled.");
    }

    @Override
    public void disable() {
        // The patch checks whether the plugin is enabled.
        LOG.info("NoSpawnerXP disabled.");
    }

    public static class SpawnerPatch extends OBlockMobSpawner {

        public SpawnerPatch() {
            super(52, 65);
            c(5.0F).a(OBlock.i).b("mobSpawner").D();
        }

        @Override
        protected void f(OWorld oworld, int i, int j, int k, int l) {
            // Check whether we're enabled.
            Plugin p = etc.getLoader().getPlugin("NoSpawnerXP");
            if (p == null || !p.isEnabled()) {
                super.f(oworld, i, j, k, l);
            }
        }
    }

}
