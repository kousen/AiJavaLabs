package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToolCallingDemoTest {

    @Test
    void travel_tools_can_be_called_directly() {
        var tools = new ToolCallingDemo.TravelTools();

        assertThat(tools.weather("Hartford")).contains("Hartford, CT");
        assertThat(tools.drivingDistance("Hartford", "Boston")).isEqualTo(102);
        assertThat(tools.fuelCost(102, 30, 3.65)).contains("$12.41");
    }
}
