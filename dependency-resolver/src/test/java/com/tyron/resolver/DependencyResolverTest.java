package com.tyron.resolver;

import com.google.common.collect.ImmutableList;
import com.tyron.common.TestUtil;
import com.tyron.resolver.model.Pom;
import com.tyron.resolver.repository.PomRepository;
import com.tyron.resolver.repository.PomRepositoryImpl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DependencyResolverTest {

    private final PomRepository repository = new PomRepositoryImpl();

    @Test
    public void testDependencyResolution() throws IOException {
        repository.addRepositoryUrl("https://repo1.maven.org/maven2");
        repository.addRepositoryUrl("https://maven.google.com");
        repository.setCacheDirectory(new File(TestUtil.getResourcesDirectory(), "cache"));
        repository.initialize();

        DependencyResolver resolver = new DependencyResolver(repository);

        Pom materialPom = repository.getPom("com.google.android.material:material:1.4.0");
        assert materialPom != null;

        List<Pom> dependencies = ImmutableList.of(materialPom);
        List<Pom> resolvedPoms =  resolver.resolve(dependencies);

        Pom newerFragment = Pom.valueOf("androidx.fragment", "fragment", "300");

        // this does not actually get the newer fragment, the equals implementation of
        // Pom is that if the artifactId and groupId is the same, it is equal
        // so this should return the older fragment
        Pom pom = resolvedPoms.get(resolvedPoms.indexOf(newerFragment));
        assert pom != null;
        assert pom.getVersionName().equals("1.1.0");

        // the version 1.4.0 of the material library depends on fragment 1.1.0
        // to test the dependency resolution, we will inject a higher version of fragment
        // and see if it gets overwritten

        dependencies = ImmutableList.of(materialPom, newerFragment);
        resolvedPoms =  resolver.resolve(dependencies);
        pom = resolvedPoms.get(resolvedPoms.indexOf(newerFragment));
        assert pom != null;
        assert pom.getVersionName().equals("300");
    }
}
