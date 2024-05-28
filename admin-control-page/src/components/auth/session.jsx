//session.jsx

export const setUserDataToSession = (user) => {
    localStorage.setItem('user', JSON.stringify(user));
  };
  
  export const getUserDataFromSession = () => {
    const userData = localStorage.getItem('user');
    return userData ? JSON.parse(userData) : null;
  };
  
  export const clearSession = () => {
    localStorage.removeItem('user');
  };
  