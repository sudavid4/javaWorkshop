import React from 'react'
import NavigationMenu from './navigationMenu.jsx'
import SystemLogin from './systemLogin.jsx'
class rootComponent extends React.Component {

    componentWillMount(){
        window.rootComponent = this;
    }

    getUsers() {
        $.get('users', (data, status) => {
            $('#response').html(_.escape(data).replace(/\n/gm, '<br>'))
        })
    }

    activePage() {
        switch(window.activeMenu) {
            case 'login':
                return <SystemLogin></SystemLogin>
            default:
                return (
                    <div>
                        <h1>Hello World Html</h1>
                        <button id="getUsers" onClick={this.getUsers}>getUsers</button>
                        <div id="response"></div>
                    </div>
                );
        }
    }

    render() {
        return (
            <div>
                <NavigationMenu></NavigationMenu>
                <div className="container">
                    {this.activePage()}
                </div>
            </div>
        )
    }
}
module.exports = rootComponent