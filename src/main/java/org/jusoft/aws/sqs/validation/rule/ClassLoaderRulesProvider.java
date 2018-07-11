package org.jusoft.aws.sqs.validation.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Implementation of the {@link RulesProvider} interface that uses a {@link ClassLoader} to find all classes implementing
 * the {@link ValidationRule} interface in the specified package. If no package is specified, the class will use the
 * default package where library provided rules are created.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ClassLoaderRulesProvider implements RulesProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderRulesProvider.class);

  private static final String VALIDATION_RULES_PACKAGE = "impl";
  private static final String CURRENT_FOLDER = ClassLoaderRulesProvider.class.getPackage().getName();
  private static final String PACKAGE_NAME_SEPARATOR = ".";
  private static final String DEFAULT_RULES_PACKAGE = CURRENT_FOLDER.concat(PACKAGE_NAME_SEPARATOR).concat(VALIDATION_RULES_PACKAGE);
  private static final String FOLDER_SEPARATOR = "/";

  private final String rulesDirectory;

  public ClassLoaderRulesProvider() {
    this(DEFAULT_RULES_PACKAGE);
  }

  public ClassLoaderRulesProvider(String rulesDirectory) {
    this.rulesDirectory = rulesDirectory;
    notNull(this.rulesDirectory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<ValidationRule> find() {
    LOGGER.info("Path to find validation rule: path={}", rulesDirectory);
    return getValidationRuleClassesFrom(rulesDirectory).stream()
      .map(toNewInstance())
      .collect(toSet());
  }

  private Function<Class<? extends ValidationRule>, ? extends ValidationRule> toNewInstance() {
    return aClass -> {
      try {
        return aClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(String.format("Unable to create instance: type=%s", aClass), e);
      }
    };
  }

  private List<Class<? extends ValidationRule>> getValidationRuleClassesFrom(String packageName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    notNull(classLoader);
    String path = packageName.replace(PACKAGE_NAME_SEPARATOR, FOLDER_SEPARATOR);
    Enumeration resources = getResources(classLoader, path);
    List<File> dirs = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = (URL) resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    List<Class<? extends ValidationRule>> classes = new ArrayList<>();
    for (File directory : dirs) {
      classes.addAll(findValidationRuleClassesFrom(directory, packageName));
    }
    return classes;
  }

  private Enumeration<URL> getResources(ClassLoader classLoader, String path) {
    try {
      return classLoader.getResources(path);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Unable to get resources: path=%s", path));
    }
  }


  private List<Class<? extends ValidationRule>> findValidationRuleClassesFrom(File directory, String packageName) {
    List<Class<? extends ValidationRule>> classes = new ArrayList<>();
    File[] files = directory.listFiles();
    if (files != null) {
      classes = Stream.of(files)
        .filter(file -> !file.isDirectory())
        .filter(this::isClassFile)
        .map(file -> getClassFrom(packageName, file))
        .filter(aClass -> !isAbstract(aClass.getModifiers()))
        .filter(aClass -> isPublic(aClass.getModifiers()))
        .filter(ValidationRule.class::isAssignableFrom)
        .map(this::toValidationRuleType)
        .collect(Collectors.toList());
    }
    return classes;
  }

  private boolean isClassFile(File file) {
    return file.getName().contains(".class");
  }

  private Class<? extends ValidationRule> toValidationRuleType(Class<?> aClass) {
    return aClass.asSubclass(ValidationRule.class);
  }

  private Class<?> getClassFrom(String packageName, File file) {
    String classNameWithoutExtension = file.getName().substring(0, file.getName().length() - 6);
    String className = packageName + PACKAGE_NAME_SEPARATOR + classNameWithoutExtension;
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format("Unable to get the class type: className=%s", className), e);
    }
  }
}
