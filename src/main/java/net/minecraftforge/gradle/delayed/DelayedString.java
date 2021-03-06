package net.minecraftforge.gradle.delayed;

import net.minecraftforge.gradle.Constants;
import net.minecraftforge.gradle.ExtensionObject;

import org.gradle.api.Project;

import groovy.lang.Closure;

@SuppressWarnings("serial")
public class DelayedString extends Closure<String>
{
    private Project project;
    private String resolved;
    private String pattern;
    private IDelayedResolver[] resolvers;

    public DelayedString(Project owner, String pattern, IDelayedResolver... resolvers)
    {
        super(owner);
        this.project = owner;
        this.pattern = pattern;
        this.resolvers = resolvers;
    }

    @Override
    public String call()
    {
        if (resolved == null)
        {
            resolved = resolve(pattern, project, resolvers);
        }
        return resolved;
    }

    public static String resolve(String patern, Project project, IDelayedResolver[] resolvers)
    {
        project.getLogger().info("Resolving: " + patern);
        ExtensionObject exten = (ExtensionObject)project.getExtensions().getByName(Constants.EXT_NAME);
        
        String build = "0";
        if (System.getenv().containsKey("BUILD_NUMBER"))
        {
            build = System.getenv("BUILD_NUMBER");
        }
        
        patern = patern.replace("{MC_VERSION}", exten.getVersion());
        patern = patern.replace("{MAIN_CLASS}", exten.getMainClass());
        patern = patern.replace("{INSTALLER_VERSION}", exten.getInstallerVersion());
        patern = patern.replace("{CACHE_DIR}", project.getGradle().getGradleUserHomeDir().getAbsolutePath() + "/caches");
        patern = patern.replace("{BUILD_DIR}", project.getBuildDir().getAbsolutePath());
        patern = patern.replace("{VERSION}", project.getVersion().toString());
        patern = patern.replace("{BUILD_NUM}", build);
        patern = patern.replace("{PROJECT}", project.getName());
        
        for (IDelayedResolver r : resolvers)
        {
            patern = r.resolve(patern, project, exten);
        }
        
        project.getLogger().info("Resolved:  " + patern);
        return patern;
    }

    public static interface IDelayedResolver
    {
        public String resolve(String patern, Project project, ExtensionObject extension);
    }
}
