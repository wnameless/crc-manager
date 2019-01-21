"use strict";

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];
            descriptor.enumerable = descriptor.enumerable || false;
            descriptor.configurable = true;
            if ("value" in descriptor) descriptor.writable = true;
            Object.defineProperty(target, descriptor.key, descriptor);
        }
    }
    return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);
        if (staticProps) defineProperties(Constructor, staticProps);
        return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

function _possibleConstructorReturn(self, call) {
    if (!self) {
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    }
    return call && (typeof call === "object" || typeof call === "function") ? call : self;
}

function _inherits(subClass, superClass) {
    if (typeof superClass !== "function" && superClass !== null) {
        throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
    }
    subClass.prototype = Object.create(superClass && superClass.prototype, {
        constructor: {
            value: subClass,
            enumerable: false,
            writable: true,
            configurable: true
        }
    });
    if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}

var Form = JSONSchemaForm.default;

var CostumFormComponent = function (_React$Component) {
    _inherits(CostumFormComponent, _React$Component);

    function CostumFormComponent(props) {
        _classCallCheck(this, CostumFormComponent);

        return _possibleConstructorReturn(this, (CostumFormComponent.__proto__ || Object.getPrototypeOf(CostumFormComponent)).call(this, props));
    }

    _createClass(CostumFormComponent, [{
        key: "render",
        value: function render() {
            return React.createElement(
                Form, {
                    id: this.props.id,
                    className: this.props.className,
                    name: this.props.name,
                    schema: this.props.schema,
                    uiSchema: this.props.uiSchema,
                    formData: this.props.formData,
                    onSubmit: this.props.onSubmit
                },
                React.createElement(
                    "div",
                    null,
                    React.createElement(
                        "button", {
                            type: "submit",
                            "class": "btn btn-primary"
                        },
                        this.props.btnText
                    )
                )
            );
        }
    }]);

    return CostumFormComponent;
}(React.Component);