package com.hubspot.blazar.discovery.docker;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;

import com.google.common.base.Optional;

public class DockerImage {
  private Optional<String> registry;
  private Optional<Integer> registryPort;
  private String image;
  private Optional<String> tag;

  public DockerImage(Optional<String> registry,
                     Optional<Integer> registryPort,
                     String image,
                     Optional<String> tag) {
    this.registry = registry;
    this.registryPort = registryPort;
    this.image = image;
    this.tag = tag;
  }

  public static DockerImage parseFromImageName(String imageName) {
    UrlValidator validator = new UrlValidator();
    Optional<String> maybeRegistry = Optional.absent();
    Optional<Integer> maybeRegistryPort = Optional.absent();
    String imagePath;
    Optional<String> maybeTag = Optional.absent();
    if (imageName.contains(":")) {
      if (imageName.contains("/") && imageName.indexOf(":") < imageName.indexOf("/")) {
        // images with a registry port
        List<String> splitOnRegistryPort = Arrays.asList(imageName.split(":", 2));
        maybeRegistry = Optional.of(splitOnRegistryPort.get(0));
        List<String> splitAfterPort = Arrays.asList(splitOnRegistryPort.get(1).split("/", 2));
        maybeRegistryPort = Optional.of(Integer.parseInt(splitAfterPort.get(0)));
        String imagePathAndMaybeTag = splitAfterPort.get(1);
        if (imagePathAndMaybeTag.contains(":")) {
          // images like 'registry.com:443/myimage' or 'registry.com:443/my/image'
          List<String> splitImageAndTag = Arrays.asList(imagePathAndMaybeTag.split(":", 2));
          imagePath = splitImageAndTag.get(0);
          maybeTag = Optional.of(splitImageAndTag.get(1));
        } else {
          // images like 'registry.com:443/myimage:1' or 'registry.com:443/my/image:1'
          imagePath = imagePathAndMaybeTag;
        }
      } else {
        // images with no registry port but with a tag
        List<String> splitOnTag = Arrays.asList(imageName.split(":"));
        maybeTag = Optional.of(splitOnTag.get(1));
        String imageWithoutTag = splitOnTag.get(0);
        if (imageWithoutTag.contains("/")) {
          List<String> splitImage = Arrays.asList(imageWithoutTag.split("/", 2));
          if (validator.isValid(String.format("http://%s", splitImage.get(0)))) {
            // image name like 'registry.com/myimage' or 'registry.com/my/image'
            maybeRegistry = Optional.of(splitImage.get(0));
            imagePath = splitImage.get(1);
          } else {
            // image name like 'my/image'
            imagePath = imageWithoutTag;
          }
        } else {
          // image name like 'myimage'
          imagePath = imageWithoutTag;
        }
      }
    } else {
      // Images with no tag or registry port
      if (imageName.contains("/")) {
        List<String> splitImage = Arrays.asList(imageName.split("/", 2));
        if (validator.isValid(String.format("http://%s", splitImage.get(0)))) {
          // image name like 'registry.com/myimage' or 'registry.com/my/image'
          maybeRegistry = Optional.of(splitImage.get(0));
          imagePath = splitImage.get(1);
        } else {
          // image name like 'my/image'
          imagePath = imageName;
        }
      } else {
        // image name like 'myimage'
        imagePath = imageName;
      }
    }
    return new DockerImage(maybeRegistry, maybeRegistryPort, imagePath, maybeTag);
  }

  public Optional<String> getRegistry() {
    return registry;
  }

  public Optional<Integer> getRegistryPort() {
    return registryPort;
  }

  public String getImage() {
    return image;
  }

  public Optional<String> getTag() {
    return tag;
  }

  private static int countOccurrences(String haystack, char needle) {
    int count = 0;
    for (int i=0; i < haystack.length(); i++)
    {
      if (haystack.charAt(i) == needle)
      {
        count++;
      }
    }
    return count;
  }

  public void setRegistry(Optional<String> registry) {
    this.registry = registry;
  }

  public void setRegistryPort(Optional<Integer> registryPort) {
    this.registryPort = registryPort;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public void setTag(Optional<String> tag) {
    this.tag = tag;
  }

  @Override
  public String toString() {
    String fullImage = "";
    if (registry.isPresent()) {
      fullImage = fullImage + registry.get();
      if (registryPort.isPresent()) {
        fullImage = fullImage + ":" + registryPort.get();
      }
      fullImage = fullImage + "/" + image;
    } else {
      fullImage = fullImage + image;
    }
    if (tag.isPresent()) {
      fullImage = fullImage + ":" + tag.get();
    }
    return fullImage;
  }
}
