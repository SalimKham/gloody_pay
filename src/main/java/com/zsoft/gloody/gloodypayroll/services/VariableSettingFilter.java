package com.zsoft.gloody.gloodypayroll.services;

import java.util.List;

public record VariableSettingFilter(String query, List<Long> groupIds) {
}
