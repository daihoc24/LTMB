import React, {useState, useEffect} from 'react';
import {View, Text, TouchableOpacity, Alert, Dimensions} from 'react-native';
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
    <View style={{flex: 1, alignItems: 'center', backgroundColor: '#ffffff'}}>
      <View style={{width: contentWidth, marginTop: 36}}>
        <View
          style={{
            width: 56,
            height: 56,
            borderRadius: 28,
            backgroundColor: '#eff6ff',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: 24,
          }}>
          <Text style={{fontSize: 28, fontWeight: '700', color: '#2563eb'}}>✓</Text>
        </View>

        <Text
          style={{
            fontSize: Math.min(screenWidth * 0.075, 28),
            lineHeight: 35,
            fontWeight: '700',
            color: '#111827',
            marginBottom: 12,
          }}>
          One last step
        </Text>

        <Text style={{fontSize: 15, lineHeight: 22, marginBottom: 28, color: '#6b7280'}}>
          By creating an account, you agree to Instagram's{' '}
          <Text style={{color: '#2563eb', fontWeight: '600'}}>Terms</Text>,{' '}
          <Text style={{color: '#2563eb', fontWeight: '600'}}>Privacy Policy</Text>{' '}
          and{' '}
          <Text style={{color: '#2563eb', fontWeight: '600'}}>Cookies Policy</Text>.
        </Text>

        <TouchableOpacity
          style={{
            backgroundColor: '#2563eb',
            minHeight: 50,
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: 25,
            marginBottom: 12,
            opacity: loading ? 0.65 : 1,
          }}
          onPress={handleRegister}
          disabled={loading}>
          <Text style={{textAlign: 'center', fontSize: 16, fontWeight: '500', color: '#ffffff'}}>
            {loading ? 'Creating account...' : 'Agree and create account'}
          </Text>
        </TouchableOpacity>

        <Text style={{textAlign: 'center', fontSize: 12, lineHeight: 18, color: '#9ca3af'}}>
          You can review these policies at any time in Settings.
        </Text>
      </View>
    </View>
  );
};

export default Register;
