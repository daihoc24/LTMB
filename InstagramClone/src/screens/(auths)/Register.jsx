import React, {useState, useEffect} from 'react';
import {View, Text, TouchableOpacity, Alert, Pressable, Dimensions} from 'react-native';
import axios from 'axios';
import ENDPOINTS from '../../config/endpoints';

const Register = ({navigation, route}) => {
  const [email, setEmail] = useState();
  const [password, setPassword] = useState();
  const [birthday, setBirthday] = useState(new Date());
  const [username, setUsername] = useState();
  const [loading, setLoading] = useState(false);
  
  // Get screen dimensions for responsive design
  const screenWidth = Dimensions.get('window').width;
  const contentWidth = Math.min(screenWidth * 0.85, 400);

  useEffect(() => {
    setEmail(route.params.email);
    setPassword(route.params.password);
    setBirthday(route.params.birthday);
    setUsername(route.params.username);

    console.log(`${email}, ${password}, ${birthday}, ${username}`)
  }, [route.params]);

  const handleRegister = async () => {

    if (!email || !password || !username || !birthday) {
      Alert.alert('Please fill in all fields');
      return;
    }

    const userCreateReq = {
      email: email,
      password: password,
      username: username,
      birthday: birthday, // Chuyển đổi ngày sinh sang định dạng ISO
    };

    try {
      setLoading(true);
      const endpoint = ENDPOINTS.USER.SIGN_IN;
      console.log(`Send request to ${endpoint} with ${userCreateReq}`);
      const response = await axios.post(endpoint, userCreateReq);

      // Check if response data exists
      if (response.data && response.data.result) {
        Alert.alert('Registration successful!', 'You can now sign in.');
        navigation.navigate('SignIn'); // Chuyển hướng đến trang đăng nhập
      } else {
        Alert.alert('Error', 'No response data received.');
      }
    } catch (error) {
      console.error(error);
      if (error.response && error.response.data) {
        Alert.alert(
          'Registration failed',
          error.response.data.message || 'An error occurred.',
        );
      } else {
        Alert.alert('Registration failed', 'An error occurred.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <View className="w-full h-full flex items-center bg-white">
      <View style={{width: contentWidth, marginTop: 16, paddingHorizontal: 16}}>
        <Text style={{fontSize: Math.min(screenWidth * 0.08, 28), fontWeight: '600', marginBottom: 4}}>
          To sign up, read and agree to our terms and policies
        </Text>
        <Text style={{fontSize: 14, marginBottom: 28, color: '#6b7280'}}>
          By signing up you agree to Instagram's Terms, Privacy Policy and
          Cookies Policy
        </Text>

        <TouchableOpacity
          style={{
            backgroundColor: '#2563eb',
            paddingVertical: 12,
            borderRadius: 24,
            marginBottom: 12,
          }}
          onPress={handleRegister}
          disabled={loading}>
          <Text style={{textAlign: 'center', fontSize: 16, fontWeight: '500', color: '#ffffff'}}>
            {loading ? 'Registering...' : 'I agree'}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default Register;
