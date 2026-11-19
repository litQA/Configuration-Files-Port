for (String stringiterator : ${(field$config)?replace("CUSTOM:", "")}Configuration.${(field$variable)?upper_case}.getStringList()) {
    ${statement$foreach}
}
