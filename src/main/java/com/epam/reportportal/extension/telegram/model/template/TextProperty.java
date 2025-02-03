package com.epam.reportportal.extension.telegram.model.template;

import java.util.Objects;

public class TextProperty implements TemplateProperty {

  private final String text;

  public TextProperty(String text) {
    this.text = text;
  }

  @Override
  public String getName() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextProperty that = (TextProperty) o;
    return text.equals(that.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text);
  }
}
