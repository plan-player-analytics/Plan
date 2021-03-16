function drawSine(canvasId) {
    // https://gist.github.com/gkhays/e264009c0832c73d5345847e673a64ab
    function drawPoint(ctx, x, y) {
        var radius = 2;
        ctx.beginPath();

        // Hold x constant at 4 so the point only moves up and down.
        ctx.arc(x - 5, y, radius, 0, 2 * Math.PI, false);

        ctx.fillStyle = '#fff';
        ctx.fill();
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    function plotSine(ctx, xOffset, yOffset) {
        var width = ctx.canvas.width;
        var height = ctx.canvas.height;

        ctx.beginPath();
        ctx.lineWidth = 2;
        ctx.strokeStyle = "#fff";

        // console.log("Drawing point...");

        var x = -2;
        var y = 0;
        var amplitude = 50;
        var frequency = 50;
        // ctx.moveTo(x, y);
        ctx.moveTo(x, 50);
        while (x <= width) {
            y = height / 2 + amplitude * Math.sin((x + xOffset) / frequency) * Math.cos((x + xOffset) / (frequency * 0.54515978463));
            ctx.lineTo(x, y);
            x += 5;
            // console.log("x="+x+" y="+y);
        }
        ctx.stroke();
        ctx.save();
        drawPoint(ctx, x, y);

        ctx.stroke();
        ctx.restore();
    }

    function draw() {
        var canvas = document.getElementById(canvasId);
        var context = canvas.getContext("2d");

        context.clearRect(0, 0, 1000, 150);
        context.save();

        plotSine(context, step, 100);
        context.restore();

        step += 0.5;
        window.requestAnimationFrame(draw);
    }

    function fix_dpi() {
        var canvas = document.getElementById(canvasId);
        let dpi = window.devicePixelRatio;//get canvas
        let ctx = canvas.getContext('2d');
        let style_width = +getComputedStyle(canvas).getPropertyValue("width").slice(0, -2);//scale the canvascanvas.setAttribute('height', style_height * dpi);
        canvas.setAttribute('width', style_width * dpi);
    }

    fix_dpi();
    var step = -1;
    window.requestAnimationFrame(draw);
}