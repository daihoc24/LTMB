import {Pressable, StyleSheet, Text, View} from 'react-native';
import React from 'react';

const NavigationBar = ({navigation, route}) => {
  return (
    <View className="w-full flex flex-row bg-white">
      <View className="w-11/12 flex flex-row items-center justify-start mx-auto mt-3">
        <Pressable className="" onPress={() => navigation.goBack()}>
          <Text style={styles.arrow_left}>←</Text>
        </Pressable>
      </View>
    </View>
  );
};

export default NavigationBar;

const styles = StyleSheet.create({
  arrow_left: {
    fontSize: 30,
    lineHeight: 36,
    color: '#111827',
  },
});
